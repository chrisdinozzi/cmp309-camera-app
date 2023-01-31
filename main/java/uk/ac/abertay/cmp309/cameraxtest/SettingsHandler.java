package uk.ac.abertay.cmp309.cameraxtest;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;

public class SettingsHandler {
    //A class to set and get the users settings
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Context context;

    //I chose to use arrays to make it easier to add more sounds or quality settings in the future
    private final String[] VALID_SHUTTERSOUND_VALUES={"shutter","quack"};
    private final String[] VALID_QUALITY_VALUES={"low","medium","high"};


    //Constructor
    public SettingsHandler(Context c){
        context = c;
        preferences = context.getSharedPreferences("photomap",0);
        editor = preferences.edit();

    }


    //Setters
    public boolean setSound(String value){
        if (Arrays.asList(VALID_SHUTTERSOUND_VALUES).contains(value)){
            editor.putString("shuttersound",value);
            editor.commit();
            return true;
        } else{
            return false;
        }
    }

    public boolean setQuality(String value){

        if (Arrays.asList(VALID_QUALITY_VALUES).contains(value)){
            editor.putString("quality",value);
            editor.commit();
            return true;
        } else{
            return false;
        }
    }

    //Getters
    public String getShutterSound(){
        return preferences.getString("shuttersound","shutter");
    }

    public String getQuality(){
        return preferences.getString("quality","medium");
    }
}
