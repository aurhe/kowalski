package com.aurhe.kowalski;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    SharedPreferences sharedPreferences;
    TextView windDirection, minutesDuration, secondsDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        sharedPreferences = getSharedPreferences("kowalski", 0);
        windDirection = (TextView) findViewById(R.id.editText1);
        minutesDuration = (TextView) findViewById(R.id.editText2);
        secondsDuration = (TextView) findViewById(R.id.editText3);

        windDirection.setText(String.valueOf(sharedPreferences.getInt("wind", 0)));
        minutesDuration.setText(String.valueOf(sharedPreferences.getInt("minutes", 38)));
        secondsDuration.setText(String.valueOf(sharedPreferences.getInt("seconds", 30)));

        Button backButton = (Button) findViewById(R.id.button1);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Editor editor = sharedPreferences.edit();

                int wind = Integer.parseInt(windDirection.getText().toString());
                int minutes = Integer.parseInt(minutesDuration.getText().toString());
                int seconds = Integer.parseInt(secondsDuration.getText()
                        .toString());

                if (wind >= 0 && wind <= 360) {
                    editor.putInt("wind", wind);
                }
                if (minutes >= 0) {
                    editor.putInt("minutes", minutes);
                }
                if (seconds >= 0 && seconds <= 60) {
                    editor.putInt("seconds", seconds);
                }

                editor.commit();

                finish();
            }
        });
    }
}
