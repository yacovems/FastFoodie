package com.ykoa.yacov.fastfoodie;

import android.content.Intent;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    static final String TAG = "LoginActivity";

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private AccessToken mAccessToken;
    private static final String EMAIL = "email";
    private String userName;
    private String userImage;
    private InternalStorageOps storage;
    private String loginFileName = "login_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storage = new InternalStorageOps();

        if (isLoggedIn(readFile())) {
            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            main.putExtra("user_name", userName);
            main.putExtra("user_image" , userImage);
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
        String status = "not logged in";
        File file = getApplicationContext().getFileStreamPath(loginFileName);

        if (file != null && file.exists()) {
            StringBuffer info = storage.
                    readFile(getApplicationContext(), loginFileName);
            String pStr = info.toString();
            String[] pLines = pStr.split("\\r?\\n");

            // Fill pending task list
            for (String s: pLines) {
                String[] words = s.split(",");
                if (!words[0].equals("")) {
                    status = words[0];
                    userName = words[1];
                    userImage = words[2];
                }
            }
        }
        return status;
    }

    private boolean isLoggedIn(String status) {

        if (status.equals("logged in")) {
            return true;
        }
        return false;
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
                            userImage = object.getJSONObject("picture")
                                    .getJSONObject("data").getString("url");
                            userName = object.getString("name");
//                            object.getString("email"));
//                            object.getString("id"));

                            String toFile = "logged in," + userName + "," + userImage;
                            storage.writeToFile(getApplicationContext(), toFile, loginFileName);

                            // After retrieving the user's info, send it to MainActivity
                            // in order to adjust the navigation drawer info.
                            Intent main = new Intent(LoginActivity.this, MainActivity.class);
                            main.putExtra("user_name", userName);
                            main.putExtra("user_image" , userImage);
                            startActivity(main);

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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode,  data);
    }
}
