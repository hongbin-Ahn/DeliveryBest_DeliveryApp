package com.example.delivery_best_d;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DeleteOrderTask extends AsyncTask<String, Void, String> {

    private DeliveryInProgressActivity activityContext;

    public DeleteOrderTask(DeliveryInProgressActivity context) {
        this.activityContext = context;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL("http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/deleteOrder.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            // Modified part
            writer.write("userID=" + params[0] + "&userAddress=" + params[1] + "&shopName=" + params[2] + "&shopAddress=" + params[3]);

            writer.flush();
            writer.close();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String response = jsonObject.getString("response");
                if ("success".equals(response)) {
                    Toast.makeText(activityContext, "주문 확인", Toast.LENGTH_SHORT).show();

                    // After order confirmation, move to MainActivity
                    Intent intent = new Intent(activityContext, MainActivity.class);
                    activityContext.startActivity(intent);
                    activityContext.finish();  // End the current Activity (optional)
                } else {
                    Toast.makeText(activityContext, "배달 완료된 내역입니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}