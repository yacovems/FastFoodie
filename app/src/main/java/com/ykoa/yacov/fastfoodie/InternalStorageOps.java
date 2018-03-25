package com.ykoa.yacov.fastfoodie;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Yacov on 3/21/2018.
 */

public class InternalStorageOps {

    public StringBuffer readFile(Context context, String fileName) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            String message;
            FileInputStream fIn = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fIn);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            while ((message = bufferedReader.readLine()) != null) {
                if (!message.equals("")){
                    stringBuffer.append(message);
                }
            }
            inputStreamReader.close();
            fIn.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public void writeToFile(final Context context, final String str, final String fileName) {

        new Thread() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                FileOutputStream fOut;
                OutputStreamWriter myOutWriter;
                try {
                    fOut = context.openFileOutput(fileName, context.MODE_PRIVATE);
                    myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.write(str);
                    myOutWriter.close();
                    fOut.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();

    }
}