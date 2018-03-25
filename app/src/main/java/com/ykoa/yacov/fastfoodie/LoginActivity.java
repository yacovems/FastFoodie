package com.ykoa.yacov.fastfoodie;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity{

    static final String TAG = "LoginActivity";

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private AccessToken mAccessToken;
    private static final String EMAIL = "email";
    private String userEmail;
    private String userName;
    private String userImage;
    private InternalStorageOps storage;
    private String loginFileName = "login_info";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storage = new InternalStorageOps();

        if (!readFile().equals("")) {
            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(main);
        }

        ImageView logo = (ImageView) findViewById(R.id.logo);
        Animation load = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        logo.startAnimation(load);

        // Facebook login
        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);

        // Button animation
        load = AnimationUtils.loadAnimation(this, R.anim.slide_in_up);
        loginButton.startAnimation(load);

        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mAccessToken = loginResult.getAccessToken();
                getUserProfile(mAccessToken);
                Log.d(TAG, "----- Logged in through facebook ------");
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "----- Log in through facebook failed ------");

            }
            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
            }
        });
    }

    private String readFile() {
        String status = "";
        File file = getApplicationContext().getFileStreamPath(loginFileName);

        if (file != null && file.exists()) {
            StringBuffer info = storage.
                    readFile(getApplicationContext(), loginFileName);
            String pStr = info.toString();
            String[] pLines = pStr.split("\\r?\\n");
            for (String s: pLines) {
                if (!s.equals("")) {
                    status = s;
                }
            }
        }
        return status;
    }

    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "Fetching user info");
                        try {
                            // Fetch user info
                            userName = object.getString("name");
                            userEmail = object.getString("email");
                            userImage = object.getJSONObject("picture")
                                    .getJSONObject("data").getString("url");
//                            object.getString("id"));

                            // Create a new user
                            HashMap<String, Object> user = new HashMap<>();
                            user.put("full name", userName);
                            user.put("first", userName.split(" ")[0]);
                            user.put("last", userName.split(" ")[1]);
                            user.put("picture", userImage);
                            user.put("email", userEmail);
                            user.put("cost", 4);
                            user.put("distance", 500);
                            user.put("rating", 2);
                            user.put("cuisine", "All");
                            user.put("favorites", new HashMap<String, String>());
                            user.put("forbidden", new HashMap<String, String>());

                            // Add user to Firebase DB
                            addUserToDB(user);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture.width(200)");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void addUserToDB(HashMap user) {
        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("User added to Firebase", "DocumentSnapshot added with ID: " + documentReference.getId());

                        // Save to internal storage
                        String toFile = documentReference.getId();
                        storage.writeToFile(getApplicationContext(), toFile, loginFileName);

                        // After retrieving the user's info, start MainActivity
                        Intent main = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(main);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode,  data);
    }
}
