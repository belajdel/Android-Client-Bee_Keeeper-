package com.chwifti.codingchallenge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.chwifti.codingchallenge.Helpers.Internet;
import com.chwifti.codingchallenge.Helpers.SessionManager;
import com.chwifti.codingchallenge.Helpers.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private int isLogin;
    private Dialog dialog;
    private Button goToBuyBt;
    private CardView warning,settings;
    private LinearLayout userData;
    private Internet internet;
    private Runnable runnable;
    private String getUserEmail;
    private SessionManager sessionManager;
    private CircleImageView userProfileImage;
    private final Handler handler = new Handler();
    private TextView username,userEmail;
    private final String API_URL = "http://192.168.121.114/chwifti_db/php/wp_main_fetch_data.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internet = new Internet(MainActivity.this);
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();


        if (sessionManager.isLogin()){
            isLogin = 1;
            realTimeFetch();

        }

        init();

        clickListener();

        HashMap<String, String> user = sessionManager.fetchUserDetail();

        getUserEmail = user.get(SessionManager.EMAIL);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void init(){

        username = findViewById(R.id.nameTv);
        userEmail = findViewById(R.id.emailTv);
        userProfileImage = findViewById(R.id.profileImage);
        warning = findViewById(R.id.warningPopUp);
        goToBuyBt = findViewById(R.id.goToBuy);
        userData = findViewById(R.id.userData);
        settings = findViewById(R.id.settingsBtn);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_dialog);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

    }

    private void clickListener() {

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();

            }
        });

        goToBuyBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlansActivity.class);
                startActivity(intent);
                finish();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void getUserData(){

        StringRequest stringRequestData = new StringRequest(Request.Method.POST, API_URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String responseData) {
                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String success = jsonObject.getString("success");
                    String successEmail = jsonObject.getString("success");
                    JSONArray jsonArray = jsonObject.getJSONArray("dataFetched");
                    if (successEmail.equals("1")){
                        if (success.equals("1")){
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject object = jsonArray.getJSONObject(i);
                                username.setText(object.getString("username"));
                                userEmail.setText(object.getString("email"));
                                int subs = object.getInt("subs");
                                String userImg = object.getString("userImage");
                                if (!isFinishing()) {
                                    Glide.with(MainActivity.this).load(userImg).into(userProfileImage);
                                }

                                if (!isFinishing()){

                                    if (subs == 1){

                                        warning.setVisibility(View.GONE);
                                        userData.setVisibility(View.VISIBLE);

                                    }else {

                                        warning.setVisibility(View.VISIBLE);
                                        userData.setVisibility(View.GONE);

                                    }

                                }

                            }
                        }else if (success.equals("0")){
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();
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
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequestData);
    }

    private void realTimeFetch(){

        handler.postDelayed(new Runnable() {
            public void run() {

                if (isLogin == 1){
                    dialog.show();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            final Handler handler = new Handler();
                            final int delay = 1000; //milliseconds
                            handler.postDelayed(new Runnable(){
                                public void run(){
                                    getUserData();
                                    dialog.dismiss();
                                    handler.postDelayed(this, delay);
                                }
                            }, delay);
                        }
                    }, 1000);
                }

            }
        }, 50);


    }

    @Override
    public void onBackPressed() {

    }

}