package com.chwifti.codingchallenge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chwifti.codingchallenge.Helpers.SessionManager;
import com.chwifti.codingchallenge.Helpers.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlansActivity extends AppCompatActivity {

    private Button buyPlan;
    private String getUserEmail;
    private ImageView backToMain;
    private SessionManager sessionManager;
    private EditText cardHolder,cardNumber,expDate,codeCvv;
    private final String Update_Data_URL = "http://192.168.121.114/chwifti_db/php/wp_updatePlan.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        init();

        clickListener();

        HashMap<String, String> user = sessionManager.fetchUserDetail();

        getUserEmail = user.get(SessionManager.EMAIL);


    }

    private void init(){

        cardHolder = findViewById(R.id.cardHolderEt);
        cardNumber = findViewById(R.id.cardNumberEt);
        expDate = findViewById(R.id.cardDateEt);
        codeCvv = findViewById(R.id.cardCvvEt);
        backToMain = findViewById(R.id.backTo);
        buyPlan = findViewById(R.id.buyBtn);

    }

    private void clickListener() {

        buyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                update_plan();

            }
        });

    }

    private void update_plan() {

        final String cardHolder = this.cardHolder.getText().toString();
        final String cardNumber = this.cardNumber.getText().toString();
        final String expDate = this.expDate.getText().toString();
        final String codeCvv = this.codeCvv.getText().toString();

        StringRequest stringRequestData = new StringRequest(Request.Method.POST, Update_Data_URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String success = jsonObject.getString("success");

                    if (success.equals("1")){

                        Intent intent = new Intent(PlansActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }else if (success.equals("0")){
                        Toast.makeText(PlansActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PlansActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("email",getUserEmail);
                params.put("cardholder", cardHolder);
                params.put("cardnumber", cardNumber);
                params.put("expdate", expDate);
                params.put("codecvv", codeCvv);

                return params;
            }
        };
        VolleySingleton.getInstance(PlansActivity.this).addToRequestQueue(stringRequestData);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PlansActivity.this, MainActivity.class));
    }



}