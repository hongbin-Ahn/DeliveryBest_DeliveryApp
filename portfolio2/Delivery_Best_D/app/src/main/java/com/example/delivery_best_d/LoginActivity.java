package com.example.delivery_best_d;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static String TAG = "phplogin";
    private static final String TAG_JSON = "user";
    private static final String TAG_ID = "dID";
    private static final String TAG_PASS = "dPassword";
    private static final String TAG_EMAIL = "demail";
    private static final String TAG_PHONE = "dphoneNumber";
    private static final String TAG_SORT = "dSort";

    private EditText mEditTextID, mEditTextPass;
    Button btn_login, btn_register;

    ArrayList<HashMap<String, String>> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditTextID = findViewById(R.id.et_id);
        mEditTextPass = findViewById(R.id.et_pass);
        btn_register = findViewById(R.id.btn_register);
        btn_login = findViewById(R.id.btn_login);

        mArrayList = new ArrayList<>();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(isNetworkConnected()) {
                    mArrayList.clear();
                    GetData task = new GetData();
                    task.execute(mEditTextID.getText().toString(), mEditTextPass.getText().toString());
                } else {
                    Toast.makeText(LoginActivity.this, "서버 오류: 인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    private class GetData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if (result == null || "error".equalsIgnoreCase(result)) {
                Toast.makeText(LoginActivity.this, "서버 오류: 로그인 실패", Toast.LENGTH_SHORT).show();
                mEditTextID.setText("");
                mEditTextPass.setText("");
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                    if (jsonArray.length() > 0) {
                        JSONObject item = jsonArray.getJSONObject(0);
                        String userID = item.getString(TAG_ID);
                        String loginSort = item.getString(TAG_SORT);

                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();

                        Intent intent;
                        if ("2".equals(loginSort)) {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        } else if ("1".equals(loginSort)) {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }

                        intent.putExtra("loginID", userID);
                        intent.putExtra("loginSort", loginSort);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        mEditTextID.setText("");
                        mEditTextPass.setText("");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "showResult: ", e);
                    Toast.makeText(LoginActivity.this, "아이디와 패스워드를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEditTextID.setText("");
                    mEditTextPass.setText("");
                }
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String dID = params[0];
            String dPassword = params[1];
            String serverURL = "http://ALB-was-878316183.ap-northeast-2.elb.amazonaws.com/Delivery/Login.php";
            String postParameters = "dID=" + dID + "&dPassword=" + dPassword;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);
                InputStream inputStream;

                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "GetData: Error ", e);
                return "error";
            }
        }
    }
}