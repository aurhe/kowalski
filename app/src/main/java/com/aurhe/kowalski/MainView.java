package com.aurhe.kowalski;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

public class MainView extends View {
    private Context context;
    private SharedPreferences sharedPreferences;
    private Timer timer;
    private boolean running = false;
    private int width, height, currentLapNumber, currentLapTime, idealLapTime, currentRaceTime,
            raceDuration, currentCompassDirection = 0, orientation = 0, windDirection = 0,
            numberOfLaps = 10;

    // texts
    private ArrayList<Text> texts;
    private Text lapNumberText, currentLapTimeText, idealLapTimeText, currentRaceTimeText,
            raceDurationText, speedText, startButtonText, settingsButtonText, previousButtonText,
            nextButtonText;

    // lap times
    private int[] lapTimes = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private TextPaint lapTimesTextPaint, lapTimesNumberTextPaint;
    private int lapNumberX, lapNumberY, lapTimesX;

    // compass
    private Path compassPath;
    private Paint compassPaint;
    private Rect compassRect;
    private int compassX, compassY;

    public MainView(Context context) {
        super(context);

        this.context = context;

        sharedPreferences = context.getSharedPreferences("kowalski", 0);

        int descriptionColor = Color.rgb(50, 50, 100),
                buttonColor = Color.rgb(200, 220, 255);

        float density = getContext().getResources().getDisplayMetrics().density;

        texts = new ArrayList<>();

        // labels
        texts.add(new Text("lap", 0.5f, 0.05f, 8, density, Paint.Align.CENTER, descriptionColor));
        texts.add(new Text("ideal lap time", 0.27f, 0.57f, 9, density, Paint.Align.RIGHT, descriptionColor));
        texts.add(new Text("current lap time", 0.46f, 0.22f, 9, density, Paint.Align.RIGHT, descriptionColor));
        texts.add(new Text("current race time", 0.95f, 0.24f, 9, density, Paint.Align.RIGHT, descriptionColor));
        texts.add(new Text("total race time", 0.95f, 0.39f, 9, density, Paint.Align.RIGHT, descriptionColor));
        texts.add(new Text("speed", 0.95f, 0.54f, 9, density, Paint.Align.RIGHT, descriptionColor));

        // buttons
        startButtonText = new Text("start", 0.075f, 0.09f, 15, density, Paint.Align.LEFT, descriptionColor, 0, 0, 0.2f, 0.15f, buttonColor);
        texts.add(startButtonText);
        settingsButtonText = new Text("settings", 0.86f, 0.09f, 15, density, Paint.Align.LEFT, descriptionColor, 0.8f, 0, 1, 0.15f, buttonColor);
        texts.add(settingsButtonText);
        previousButtonText = new Text("previous", 0.1f, 0.925f, 15, density, Paint.Align.LEFT, descriptionColor, 0, 0.8f, 0.3f, 1, buttonColor);
        texts.add(previousButtonText);
        nextButtonText = new Text("next", 0.725f, 0.875f, 15, density, Paint.Align.LEFT, descriptionColor, 0.5f, 0.7f, 1, 1, buttonColor);
        texts.add(nextButtonText);

        // variable text
        lapNumberText = new Text("--", 0.5f, 0.18f, 50, density, Paint.Align.CENTER, Color.BLACK);
        texts.add(lapNumberText);
        currentLapTimeText = new Text("--", 0.46f, 0.45f, 90, density, Paint.Align.RIGHT, Color.BLACK);
        texts.add(currentLapTimeText);
        idealLapTimeText = new Text("--", 0.27f, 0.67f, 35, density, Paint.Align.RIGHT, Color.BLACK);
        texts.add(idealLapTimeText);
        currentRaceTimeText = new Text("--", 0.95f, 0.3f, 20, density, Paint.Align.RIGHT, Color.BLACK);
        texts.add(currentRaceTimeText);
        raceDurationText = new Text("--", 0.95f, 0.45f, 20, density, Paint.Align.RIGHT, Color.BLACK);
        texts.add(raceDurationText);
        speedText = new Text("--", 0.95f, 0.6f, 20, density, Paint.Align.RIGHT, Color.BLACK);
        texts.add(speedText);

        lapTimesTextPaint = initTextPaint(15, density, Paint.Align.RIGHT, Color.BLACK);
        lapTimesNumberTextPaint = initTextPaint(10, density, Paint.Align.RIGHT, descriptionColor);

        compassPaint = new Paint();
        compassPaint.setColor(android.graphics.Color.RED);
        compassPaint.setStyle(Paint.Style.FILL);
        compassPaint.setAntiAlias(true);

        setBackgroundColor(Color.WHITE);

        resetTimes(false);
    }

    private TextPaint initTextPaint(int size, float density, Align align, int color) {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextAlign(align);
        textPaint.setTextSize(size * density);
        textPaint.setColor(color);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        return textPaint;
    }

    public void resetTimes(boolean clearStoredData) {

        raceDuration = (sharedPreferences.getInt("minutes", 38) * 60) + sharedPreferences.getInt("seconds", 30);
        windDirection = sharedPreferences.getInt("wind", 0);

        if (clearStoredData) {
            Editor editor = sharedPreferences.edit();

            currentLapNumber = 0;
            editor.putInt("currentLapNumber", 0);
            currentRaceTime = 0;
            editor.putInt("currentRaceTime", 0);
            currentLapTime = 0;

            lapTimes = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
            for (int i = 0; i < numberOfLaps; i++) {
                editor.putInt("lap" + i, -1);
            }
            editor.commit();
        } else {
            currentLapNumber = sharedPreferences.getInt("currentLapNumber", 0);
            currentRaceTime = sharedPreferences.getInt("currentRaceTime", 0);
            currentLapTime = 0;

            for (int i = 0; i < numberOfLaps; i++) {
                lapTimes[i] = sharedPreferences.getInt("lap" + i, -1);
            }
        }

        if (numberOfLaps != currentLapNumber) {
            idealLapTime = (raceDuration - currentRaceTime) / (numberOfLaps - currentLapNumber);
        } else {
            idealLapTime = 0;
        }
    }

    private void startRace() {
        if (running) {
            resetTimes(true);
            timer.cancel();
            startButtonText.setText("start");
            running = false;
        } else {
            resetTimes(false);
            startTimer();
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentLapTime++;
                currentRaceTime++;
                postInvalidate();
            }
        }, 0, 1000);
        startButtonText.setText("reset");
        running = true;
    }

    private void nextLap() {
        if (currentLapNumber < numberOfLaps - 1) {
            lapTimes[currentLapNumber] = currentLapTime;
            sharedPreferences.edit().putInt("lap" + currentLapNumber, currentLapTime).commit();

            currentLapNumber++;
            sharedPreferences.edit().putInt("currentLapNumber", currentLapNumber).commit();

            idealLapTime = (raceDuration - currentRaceTime) / (numberOfLaps - currentLapNumber);

            currentLapTime = 0;
        } else if (currentLapNumber == numberOfLaps - 1) {
            lapTimes[currentLapNumber] = currentLapTime;
            sharedPreferences.edit().putInt("lap" + currentLapNumber, currentLapTime).commit();

            currentLapNumber = numberOfLaps;
            sharedPreferences.edit().putInt("currentLapNumber", currentLapNumber).commit();

            timer.cancel();
        }

        sharedPreferences.edit().putInt("currentRaceTime", currentRaceTime).commit();
    }

    private void previousLap() {
        if (currentLapNumber > 0) {
            currentLapNumber--;
            sharedPreferences.edit().putInt("currentLapNumber", currentLapNumber).commit();

            currentLapTime = lapTimes[currentLapNumber];

            lapTimes[currentLapNumber] = -1;
            sharedPreferences.edit().putInt("lap" + currentLapNumber, -1).commit();

            idealLapTime = (raceDuration - currentRaceTime) / (numberOfLaps - currentLapNumber);
            if (currentLapNumber == numberOfLaps - 1) {
                startTimer();
            }

            sharedPreferences.edit().putInt("currentRaceTime", currentRaceTime).commit();
        }
    }

    private String getTime(int seconds) {
        int sec = seconds % 60;
        return seconds / 60 + ":" + (sec < 10 ? "0" : "") + sec;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        compassX = (int) (width * 0.65f);
        compassY = (int) (height * 0.4f);

        int halfCompassWidth = (int) (height * 0.02),
                halfCompassHeight = (int) (height * 0.175);

        compassPath = new Path();
        compassPath.setFillType(Path.FillType.EVEN_ODD);
        compassPath.moveTo(compassX, compassY - halfCompassHeight);
        compassPath.lineTo(compassX + halfCompassWidth, compassY + halfCompassHeight);
        compassPath.lineTo(compassX - halfCompassWidth, compassY + halfCompassHeight);
        compassPath.lineTo(compassX, compassY - halfCompassHeight);
        compassPath.close();

        compassRect = new Rect(
                compassX - halfCompassHeight,
                compassY - halfCompassHeight,
                compassX + halfCompassHeight,
                compassY + halfCompassHeight);

        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).updatePosition(width, height);
        }

        lapNumberY = (int) (height * 0.48f);
        lapNumberX = (int) (width * 0.38f);
        lapTimesX = (int) (width * 0.45f);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // update values
        if (currentLapNumber >= numberOfLaps) {
            lapNumberText.setText("--");
        } else {
            lapNumberText.setText(String.valueOf(currentLapNumber + 1));
        }
        currentLapTimeText.setText(getTime(currentLapTime));
        idealLapTimeText.setText(getTime(idealLapTime));
        currentRaceTimeText.setText(getTime(currentRaceTime));
        raceDurationText.setText(getTime(raceDuration));

        // draw texts
        for (int i = 0; i < texts.size(); i++) {
            texts.get(i).draw(canvas);
        }

        // draw lap times
        float posY = lapNumberY;
        for (int i = numberOfLaps - 1; i >= 0; i--) {
            if (lapTimes[i] >= 0) {
                posY += height * 0.05f;
                canvas.drawText((i + 1) + "", lapNumberX, posY, lapTimesNumberTextPaint);
                canvas.drawText(getTime(lapTimes[i]), lapTimesX, posY, lapTimesTextPaint);
            }
        }

        // draw compass
        if ((orientation + windDirection) > (currentCompassDirection + 2)
                || (orientation + windDirection) < (currentCompassDirection - 2)) {
            currentCompassDirection = orientation + windDirection;
        }
        canvas.rotate(currentCompassDirection, compassX, compassY);
        canvas.drawPath(compassPath, compassPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (nextButtonText.contains(event.getX(), event.getY())) {
            nextLap();
        } else if (previousButtonText.contains(event.getX(), event.getY())) {
            previousLap();
        } else if (startButtonText.contains(event.getX(), event.getY())) {
            startRace();
        } else if (settingsButtonText.contains(event.getX(), event.getY())) {
            Intent intent = new Intent(context, SettingsActivity.class);
            ((Activity) context).startActivityForResult(intent, 0);
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    public void setOrientation(int value) {
        orientation = value;
        if (compassRect != null) {
            invalidate(compassRect);
        }
    }

    public void setSpeed(int value) {
        speedText.setText(value + " km/h");
        invalidate();
    }
}
