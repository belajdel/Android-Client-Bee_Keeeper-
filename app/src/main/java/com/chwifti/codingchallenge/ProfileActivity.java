package com.chwifti.codingchallenge;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.chwifti.codingchallenge.Helpers.RandomString;
import com.chwifti.codingchallenge.Helpers.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private CircleImageView profileImageEt;
    private TextView nameTv, emailTv, shareTv, logoutBtn, geoCodeTV;
    private ImageView backToMain;
    private Button updateBtn;
    private final Handler handler = new Handler();

    private SessionManager sessionManager;
    private Bitmap bitmap;

    private String getUserEmail,getLoc,randomId,
            API_URL = "http://192.168.121.114/chwifti_db/php/wp_main_fetch_data.php",
            UPDATE_URL = "http://192.168.121.114/chwifti_db/php/wp_upload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        init();

        getUserData();

        clickListener();

        HashMap<String, String> user = sessionManager.fetchUserDetail();

        getUserEmail = user.get(SessionManager.EMAIL);


        TelephonyManager tm = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();

        if (countryCodeValue != null) {
            Locale loc = new Locale("",countryCodeValue);

            getLoc = String.valueOf(loc);

        }

        geoCodeTV.setText(getLoc);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    private void init(){

        profileImageEt = findViewById(R.id.profileImage);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        shareTv = findViewById(R.id.shareTv);
        logoutBtn = findViewById(R.id.logoutTv);
        updateBtn = findViewById(R.id.updateBtn);
        geoCodeTV = findViewById(R.id.geoCode);
        backToMain = findViewById(R.id.backTo);

    }

    private void clickListener() {

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.logOut();
            }
        });

        profileImageEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseFile();

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateImage(getUserEmail,getStringImage(bitmap));
                Generate();

            }
        });

        shareTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String shareBody = "Check out the best earning app. Download " + getString(R.string.app_name) +
                        " from Play Store\n" +
                        "https://play.google.com/store/apps/details?id=" +
                        getPackageName();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                intent.setType("text/plain");
                startActivity(intent);

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

                                nameTv.setText(object.getString("username"));
                                emailTv.setText(object.getString("email"));
                                String userImg = object.getString("userImage");

                                Glide.with(ProfileActivity.this).load(userImg).into(profileImageEt);

                            }

                        }else if (success.equals("0")){

                            Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();

                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "Error " +e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(ProfileActivity.this, "Error " +error.toString(), Toast.LENGTH_SHORT).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<>();
                params.put("email",getUserEmail);
                return params;
            }
        };

        RequestQueue requestQueueData = Volley.newRequestQueue(this);
        requestQueueData.add(stringRequestData);

    }

    private void chooseFile(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){

            Uri filePath = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileImageEt.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

            updateBtn.setVisibility(View.VISIBLE);

        }else{

            updateBtn.setVisibility(View.GONE);

        }

    }

    private void updateImage(final String getUserEmail, final String userImage) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i(TAG, response.toString());

                try {
                    JSONObject jsonObjectPic = new JSONObject(response);
                    String success = jsonObjectPic.getString("success");
                    String failure = jsonObjectPic.getString("success");

                    if (success.equals("1")){

                        updateBtn.setVisibility(View.GONE);

                    }else if (failure.equals("0")){

                        Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        updateBtn.setVisibility(View.GONE);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "Try Again" +e.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(ProfileActivity.this, "Try Again " +error.toString(), Toast.LENGTH_SHORT).show();

            }
        })
        {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> params = new HashMap<>();
                params.put("email", getUserEmail);
                params.put("userimage", userImage);
                params.put("rand", randomId);

                return params;

            }

        };

        RequestQueue requestQueueData = Volley.newRequestQueue(this);
        requestQueueData.add(stringRequest);


    }

    public String getStringImage(Bitmap bitmap){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageByteArray = byteArrayOutputStream.toByteArray();

        String encodedImage = Base64.encodeToString(imageByteArray, Base64.DEFAULT);

        return encodedImage;
    }

    private void Generate(){

        RandomString randomString = new RandomString();

        randomId = randomString.generateAlphaNumeric(10);

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }


}