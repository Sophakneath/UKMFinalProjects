package com.example.phakneath.jobber.sharePreferences;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private static final String PASSWORD = "PASSWORD";

    private static final String USER_PREFERENCE= "USER_PREFERECNCE";

    private static SharedPreferences getPreference(Context context)
    {
        return context.getSharedPreferences(USER_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static void save(Context context, String password)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(PASSWORD, password);
        editor.commit();
    }

    public static String getPassword(Context context)
    {
        SharedPreferences preferences = getPreference(context);
        return  preferences.getString(PASSWORD,null);
    }

    public static void remove(Context context)
    {
        SharedPreferences.Editor editor = getPreference(context).edit();
        editor.putString(PASSWORD,null);
        editor.commit();
    }
}
