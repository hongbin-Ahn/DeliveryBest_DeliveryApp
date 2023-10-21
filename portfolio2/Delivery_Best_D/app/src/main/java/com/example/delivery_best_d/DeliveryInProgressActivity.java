package com.example.delivery_best_d;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DeliveryInProgressActivity extends AppCompatActivity {

    private Button btnDeliveryComplete;
    private String userID, userAddress, shopName, shopAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_in_progress);

        btnDeliveryComplete = findViewById(R.id.btn_delivery_complete);

        // Get order details from intent
        userID = getIntent().getStringExtra("userID");
        userAddress = getIntent().getStringExtra("userAddress");
        shopName = getIntent().getStringExtra("shopName");
        shopAddress = getIntent().getStringExtra("shopAddress");

        btnDeliveryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOrderFromDB(userID, userAddress, shopName, shopAddress);
            }
        });
    }

    private void deleteOrderFromDB(String userID, String userAddress, String shopName, String shopAddress) {
        new DeleteOrderTask().execute(userID, userAddress, shopName, shopAddress);
    }

    private class DeleteOrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/deleteFromOrdering.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                // Modified part
                String postData = "userID=" + params[0] + "&userAddress=" + params[1] + "&shopName=" + params[2] + "&shopAddress=" + params[3];

                writer.write(postData);
                writer.flush();
                writer.close();
                outputStream.close();

                StringBuilder response = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();

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

                    if(response.equals("success")) {
                        Toast.makeText(DeliveryInProgressActivity.this, "배달 완료", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(DeliveryInProgressActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(DeliveryInProgressActivity.this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DeliveryInProgressActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}