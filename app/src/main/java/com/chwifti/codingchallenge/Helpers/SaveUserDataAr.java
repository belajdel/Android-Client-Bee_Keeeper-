package com.chwifti.codingchallenge.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveUserDataAr {

    Context context;
    String saveName;
    static SharedPreferences sp;

    public SaveUserDataAr(Context context, String saveName) {
        this.context = context;
        this.saveName = saveName;
        sp = context.getSharedPreferences(saveName,context.MODE_PRIVATE);
    }

    public static void setState(int key){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("Key",key);
        editor.apply();
    }

    public int getState(){
        return sp.getInt("Key",0);
    }

}
