package com.example.fallinggrandpa;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAGNOTIF = "Notif";
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final int MULTIPLE_PERMISSIONS = 1;
    private static final String FILE_NAME = "save.json";
    private static final int MY_PERMISSION_REQUEST_CODE_SEND_SMS = 2;
    protected Button btnAddNumber;
    final String TAGSensor = " sensor ";
    final String TAGSMS = "Sms";
    final String TAGPerm = "Permissions";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private NotificationHelper notifHelp;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    double criticalValue = 20;
    private static final int TIME_CLOSE = 10000; // Ms
    private double latitude ;
    private double longitude;
    private final String[] permissions= new String[]{Manifest.permission.VIBRATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAddNumber = findViewById(R.id.buttonAddNumber);
        PhoneView.add(this, btnAddNumber);
        sensorDetection();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        }
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        createLocationRequest();
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                if(locationResult == null)
                {
                    Log.v(TAGPerm, "location == null, failed to update");
                }
                for(Location location : locationResult.getLocations())
                {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.v(TAGPerm, "Position updated Latitude : " + latitude + "][Longitude : " + longitude);
                }
            }
        };
        /*authorizeLocation();
        askPermissionAndSendSMS();*/
        askPerm();
        try {
            readData();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            saveData(new View(MainActivity.this));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }


    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void sensorDetection() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        if (deviceSensors != null && !deviceSensors.isEmpty()) {
            for (Sensor mySensor : deviceSensors) {
                Log.v(TAGSensor, " info : " + mySensor.toString());
            }
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                Log.v(TAGSensor, " info : Accelerometer found ! ");
            } else {
                Log.v(TAGSensor, " info : Accelerometer not found ! ");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            final FusedLocationProviderClient mFusedLoactionClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            mFusedLoactionClient.getLocationAvailability();

            mFusedLoactionClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());


            mFusedLoactionClient.getLastLocation()
                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                        Log.v(TAGPerm, "Position updated Latitude : " + latitude + "][Longitude : " + longitude);
                                    } else {
                                        //make Location request
                                        Log.v(TAGPerm, "location == null, make request");
                                    }
                                }
                            }
                    );
        } else {
            Log.v(TAGPerm, "getLocation() : Permission Not Granted");
        }
    }



    private void askPerm()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // 23
            // Check if we have send SMS permission
            int result;
            List<String> listPermissionsNeeded = new ArrayList<>();
            // Check if we have send SMS permission
            for (String p:permissions) {
                result = this.checkSelfPermission(p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                this.requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        Log.v(TAGPerm, "Perm not granted.");
                        Toast.makeText(this, "Permission refusée!", Toast.LENGTH_LONG).show();
                        new AlertDialog.Builder(this)
                                .setTitle("Permission requise")
                                .setMessage("Vous devez autoriser cette permission pour accéder à l'application")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                        System.exit(0);
                                    }
                                })
                                .create()
                                .show();
                    } else {
                        Log.v(TAGPerm, "REQUEST_CODE_LOCATION granted.");
                        Toast.makeText(this, "Permission accordée!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public final void onSensorChanged(SensorEvent event) {
        float Ax = event.values[0];
        float Ay = event.values[1];
        float Az = event.values[2];
        double accGlobal = java.lang.Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
        //Log.v(TAGSensor, "accGlobal = " + accGlobal);
        if (accGlobal > criticalValue) {
            displayWarning();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayWarning() {
        getLocation();
        if (notifHelp == null) {
            notifHelp = new NotificationHelper(MainActivity.this);
        }
        if (notifHelp.notificationmanager.getActiveNotifications().length == 0) {
            Log.v(TAGSMS, "WARNING ! Fall ?");
            notifHelp.notify(this,1, true, "Attention !", "Avez-vous fait une chute ?");
            (new Handler()).postDelayed(new Runnable() {
                public void run() {
                    try {
                        notifHelp.canceled(MainActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, TIME_CLOSE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void sendtoContacts() throws IOException {
        LinearLayout scrollViewlinerLayout = this.findViewById(R.id.LayoutNumbers);
        getLocation();
        for (int i = 0; i < scrollViewlinerLayout.getChildCount(); i++) {
            ConstraintLayout innerLayout = (ConstraintLayout) scrollViewlinerLayout.getChildAt(i);
            EditText edit = innerLayout.findViewById(R.id.editTextPhone);
            sendSms(edit.getText().toString());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendSms(String number) throws IOException {
        if (validPhoneNumber(number)) {
            //Send SMS here
            Log.v(TAGNOTIF, "Sent SMS to : " + number);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = "Error";
            try {
                address = addresses.get(0).getAddressLine(0);

            }
            catch (java.lang.IndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
            String message = "Envois automatique de message :\nIl semblerait que je sois tombé à l'adresse " + address + ". Je vous demande votre aide.";
            try {
                // Get the default instance of the SmsManager
                SmsManager smsManager = SmsManager.getDefault();
                // Send Message
                smsManager.sendTextMessage(number,
                        null,
                        message,
                        null,
                        null);

                Log.i( TAGNOTIF,"Your sms has successfully sent!");
                Toast.makeText(getApplicationContext(),"Your sms has successfully sent!",
                        Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                Log.e( TAGNOTIF,"Your sms has failed...", ex);
                Toast.makeText(getApplicationContext(),"Your sms has failed... " + ex.getMessage(),
                        Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }


            Log.v(TAGSMS, "Adress would be : " + address + "\n");

        } else {
            Log.v(TAGNOTIF, "Not a valid number : " + number);
        }
    }

    public void saveData(View view) throws JSONException, IOException {
        //remove keyboard
        InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        LinearLayout scrollViewlinerLayout = this.findViewById(R.id.LayoutNumbers);
        File file = new File(MainActivity.this.getFilesDir(), FILE_NAME);
        FileWriter fileW = new FileWriter(file);
        BufferedWriter buffW = new BufferedWriter(fileW);
        for (int i = 0; i < scrollViewlinerLayout.getChildCount(); i++) {
            ConstraintLayout innerLayout = (ConstraintLayout) scrollViewlinerLayout.getChildAt(i);
            EditText edit = innerLayout.findViewById(R.id.editTextPhone);
            String phone = edit.getText().toString();
            if (validPhoneNumber(phone)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("phone", phone);
                buffW.write(jsonObject.toString());
                buffW.write(",");
                Log.v(TAGNOTIF, "Phone : "+ phone);
            }
        }
        buffW.close();
    }

    private void readData() throws IOException, JSONException {
        File file = new File(MainActivity.this.getFilesDir(), FILE_NAME);
        FileReader fileR = new FileReader(file);
        BufferedReader buffR = new BufferedReader(fileR);
        StringBuilder sbuild = new StringBuilder();
        String line = buffR.readLine();
        Log.v(TAGNOTIF, "line : "+ line);
        while (line != null) {
            sbuild.append(line);
            line = buffR.readLine();
        }
        buffR.close();
        String[] listS = sbuild.toString().split(",");
        for (String list : listS) {
            addNumbers(new JSONObject(list));
        }

    }

    private void addNumbers(JSONObject jsonObject) throws JSONException {
        String num = (String) jsonObject.get("phone");
        PhoneView.create(MainActivity.this, num);
    }

    private boolean validPhoneNumber(String number) {
        return !(number.length() > 13 | number.length() < 6 | !PhoneNumberUtils.isGlobalPhoneNumber(number));
    }

}