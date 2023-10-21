package com.example.delivery_best_d;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView orderListView;
    private ArrayAdapter<String> orderAdapter;
    private List<String> orderDataList;
    private Button btnLogout;

    private String[] orderParts; // 클래스 레벨에서 선언

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        orderListView = findViewById(R.id.orderListView);
        btnLogout = findViewById(R.id.btnLogout);
        orderDataList = new ArrayList<>();
        orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderDataList);
        orderListView.setAdapter(orderAdapter);

        new GetOrderData().execute();

        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String selectedOrder = orderDataList.get(position);
                orderParts = selectedOrder.split(" - |\n");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("주문 수락");
                builder.setMessage("주문하시겠습니까?");

                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CopyOrderToOrderingTask().execute(orderParts[0], orderParts[1], orderParts[2], orderParts[3]);
                        new DeleteOrderFromShopOrderTask().execute(orderParts[0], orderParts[1], orderParts[2], orderParts[3]);

                        Intent intent = new Intent(MainActivity.this, DeliveryInProgressActivity.class);
                        intent.putExtra("userID", orderParts[0]);
                        intent.putExtra("userAddress", orderParts[1]);
                        intent.putExtra("shopName", orderParts[2]);
                        intent.putExtra("shopAddress", orderParts[3]);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetOrderData().execute();
    }

    private void logoutUser() {
        Toast.makeText(MainActivity.this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private class GetOrderData extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return fetchData("http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/shopOrder.php");
        }

        @Override
        protected void onPostExecute(String result) {
            orderDataList.clear();

            Log.d("API_RESPONSE", result);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray("response");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject order = jsonArray.getJSONObject(i);
                        String userID = order.getString("userID");
                        String userAddress = order.getString("userAddress");
                        String shopName = order.getString("shopName");
                        String shopAddress = order.getString("shopAddress");
                        String orderInfo = userID + "\n" + userAddress + "\n" + shopName + "\n" + shopAddress;
                        orderDataList.add(orderInfo);
                    }
                    orderAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class CopyOrderToOrderingTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return postData("http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/copyOrder.php", params);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String response = jsonObject.getString("response");
                    if ("success".equals(response)) {
                        Toast.makeText(MainActivity.this, "Order copied successfully", Toast.LENGTH_SHORT).show();
                        new DeleteOrderFromShopOrderTask().execute(orderParts[0], orderParts[1], orderParts[2], orderParts[3]);
                    } else {
                        Toast.makeText(MainActivity.this, "Error copying order", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DeleteOrderFromShopOrderTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return postData("http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/deleteOrder.php", params);
        }
    }

    private String postData(String urlString, String... params) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            // 여기서 인코딩된 매개변수를 전송하도록 수정합니다.
            StringBuilder postData = new StringBuilder();
            postData.append("userID=").append(params[0]);
            postData.append("&userAddress=").append(params[1]);
            postData.append("&shopName=").append(params[2]);
            postData.append("&shopAddress=").append(params[3]);

            writer.write(postData.toString());

            writer.flush();
            writer.close();
            os.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            connection.disconnect();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String fetchData(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            connection.disconnect();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}