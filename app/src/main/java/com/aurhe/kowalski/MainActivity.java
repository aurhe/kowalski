package com.aurhe.kowalski;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity implements SensorEventListener, LocationListener {
    private SensorManager sensorManager;
    private Sensor accelerometer, magneticField;
    private MainView mainView;

    private float[] valuesAccelerometer = new float[3];
    private float[] valuesMagneticField = new float[3];
    private float[] matrixR = new float[9];
    private float[] matrixI = new float[9];
    private float[] matrixValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainView = new MainView(this);
        setContentView(mainView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magneticField);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                valuesAccelerometer = event.values;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                valuesMagneticField = event.values;
                break;
        }

        SensorManager.getRotationMatrix(matrixR, matrixI, valuesAccelerometer, valuesMagneticField);
        SensorManager.getOrientation(matrixR, matrixValues);

        mainView.setOrientation(Math.round(-matrixValues[0] * 360 / (float) (2 * Math.PI) + 90));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainView.resetTimes(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        mainView.setSpeed(Math.round(location.getSpeed() / 1000 * 3600));
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
