package com.chwifti.codingchallenge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chwifti.codingchallenge.Helpers.RandomString;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    private TextView loginTV;
    private Button registerBtn;
    private EditText nameEdit,emailEdit,passwordEdit,confirmPasswordEdit;
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";

    private String userName, email, password, confirm_Pass,randomKey,refCode,otp,
            REG_URL = "http://192.168.121.114/chwifti_db/Php/wp_register.php",
            OTP_URL = "http://192.168.121.114/chwifti_db/php/wp_sendOtp.php",
            CHECK_OTP_URL = "http://192.168.121.114/chwifti_db/php/wp_checkOtp.php";

    private Random randomCode = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        init();
        clickListener();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void init() {
        registerBtn = findViewById(R.id.registerBtn);
        nameEdit = findViewById(R.id.userNameET);
        emailEdit = findViewById(R.id.userEmailET);
        passwordEdit = findViewById(R.id.passwordET);
        confirmPasswordEdit = findViewById(R.id.confirmPasswordET);
        loginTV = findViewById(R.id.login);

    }


    private void clickListener() {

        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                userName = nameEdit.getText().toString().trim();
                email = emailEdit.getText().toString().trim();
                password = passwordEdit.getText().toString().trim();
                confirm_Pass = confirmPasswordEdit.getText().toString().trim();

                int length = userName.length();
                int length_pass = password.length();

                if (userName.isEmpty()) {

                    nameEdit.setError("Required");

                }else if (length < 5) {

                    nameEdit.setError("Username Too Short");

                }else if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {

                    emailEdit.setError("Required");

                }else if (password.isEmpty()) {

                    passwordEdit.setError("Required");

                }else if (length_pass < 6) {

                    passwordEdit.setError("Password  is Too Short");

                }else if (confirm_Pass.isEmpty() || !password.equals(confirm_Pass)) {

                    confirmPasswordEdit.setError("Invalid Password");

                }else {

                    Generate();
                    GenerateId();
                    GenerateOtp();
                    Register();

                }

            }
        });

    }

    private void Register(){

        final String username = this.nameEdit.getText().toString();
        final String email = this.emailEdit.getText().toString().trim();
        final int emailStatus = 0;
        final String password = this.passwordEdit.getText().toString();
        final String confirm_password = this.confirmPasswordEdit.getText().toString();
        final String user_account_key = randomKey;
        final String otpCode = otp;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    int responseDb = jsonObject.getInt("success");
                    int responseDbE = jsonObject.getInt("success");

                    if (responseDbE == 0){

                        Toast.makeText(SignupActivity.this, "This User Already Registered", Toast.LENGTH_SHORT).show();

                    }else if (responseDb == 1){
                        SendOtp();
                        VerifyAccount();

                    }else{

                        Toast.makeText(SignupActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SignupActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(SignupActivity.this, "Error " +error.toString(), Toast.LENGTH_SHORT).show();

            }
        }){

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String>params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("emailStatus", String.valueOf(emailStatus));
                params.put("password", password);
                params.put("confPassword", confirm_password);
                params.put("emailKey", user_account_key);
                params.put("otpCode", otpCode);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void SendOtp(){
        final String email = this.emailEdit.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, OTP_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    int responseDb = jsonObject.getInt("success");
                    int responseDbE = jsonObject.getInt("success");

                    if (responseDbE == 0){

                        Toast.makeText(SignupActivity.this, "Something Wrong", Toast.LENGTH_SHORT).show();

                    }else if (responseDb == 1){

                        Toast.makeText(SignupActivity.this, "Please Verify Your Email", Toast.LENGTH_SHORT).show();
                    }else{

                        Toast.makeText(SignupActivity.this, "Error", Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(SignupActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(SignupActivity.this, "Error " +error.toString(), Toast.LENGTH_SHORT).show();

            }
        }){

            @NonNull
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String>params = new HashMap<>();
                params.put("email", email);

                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void VerifyAccount(){

        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.otp_popup, null);
        EditText otpCodeBox = dialogView.findViewById(R.id.otpBox);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSend = dialogView.findViewById(R.id.btnSend);


        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = SignupActivity.this.emailEdit.getText().toString().trim();
                final String getOtpCodeBox = otpCodeBox.getText().toString().trim();

                if (getOtpCodeBox.isEmpty()){

                    otpCodeBox.setError("Required");

                }else{

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, CHECK_OTP_URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {

                                JSONObject jsonObject = new JSONObject(response);
                                int responseDb = jsonObject.getInt("success");

                                if (responseDb == 1){

                                    dialog.dismiss();
                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                }else{

                                    otpCodeBox.setError("Wrong OTP Code");

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SignupActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();

                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            Toast.makeText(SignupActivity.this, "Error " +error.toString(), Toast.LENGTH_SHORT).show();

                        }
                    }){

                        @NonNull
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            Map<String, String>params = new HashMap<>();
                            params.put("email", email);
                            params.put("otp", getOtpCodeBox);

                            return params;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(SignupActivity.this);
                    requestQueue.add(stringRequest);

                }

            }
        });


    }

    private void Generate(){

        RandomString randomString = new RandomString();

        randomKey = randomString.generateAlphaNumeric(200);

    }

    private void GenerateId(){

        RandomString randomString = new RandomString();
        refCode = randomString.generateId(7);

    }

    private void GenerateOtp(){

        RandomString randomString = new RandomString();

        otp = randomString.generateNumeric(6);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


}