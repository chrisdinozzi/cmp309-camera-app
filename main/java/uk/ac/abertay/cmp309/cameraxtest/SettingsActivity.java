package uk.ac.abertay.cmp309.cameraxtest;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;


//This activity allows users to customise some aspects of their experience.
// They can choose how they want their images saved, either png or jpg,
// and they can choose a shutter noise, either a classic shutter sound or a fun duck quack :D

public class SettingsActivity extends AppCompatActivity {
    String quality;
    String shuttersound;
    SettingsHandler settingsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsHandler = new SettingsHandler(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String sShuttersound = settingsHandler.getShutterSound();
        if (sShuttersound.equals("quack")){//if shuttersound is shutter
            //check shutter button
            RadioButton rbSound = findViewById(R.id.rbQuack);
            rbSound.setChecked(true);
        }

        String sQuality = settingsHandler.getQuality();
        RadioButton rbQuality = null;
        switch (sQuality){
            case "low":
                rbQuality = findViewById(R.id.rbLow);
                break;
            case "medium":
                rbQuality = findViewById(R.id.rbMedium);
                break;
            case "high":
                rbQuality = findViewById(R.id.rbHigh);
                break;
        }
        rbQuality.setChecked(true);
    }


    //IMAGE QUALITY

    public void onLowRadioClicked (View v){
        //called when users selects low button
        quality = "low";
        settingsHandler.setQuality(quality);
    }
    public void onMediumRadioClicked (View v){
        //called when user selects medium button
        quality = "medium";
        settingsHandler.setQuality(quality);
    }
    public void onHighRadioClicked (View v){
        //called when user selects high button
        quality = "high";
        settingsHandler.setQuality(quality);
    }


    //SHUTTER NOISE

    public void onShutterRadioClicked (View v){
        //called when user selects shutter sound button
        shuttersound = "shutter";
        settingsHandler.setSound(shuttersound);
    }

    public void onQuackRadioClicked (View v){
        //called when user selects quack sound button
        shuttersound = "quack";
        settingsHandler.setSound(shuttersound);
    }
}