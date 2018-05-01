package com.example.mbientlab.firealert;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;

import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Temperature.SensorType;

import bolts.Continuation;
import bolts.Task;

import com.mbientlab.metawear.android.BtleService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * inheriting from AppCompatActivity class and implementing
 * the interface ServiceConnection which will help us to monitor
 * the state of our application service
 */
public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;        // we will use serviceBinder to interact with the service
    static MetaWearBoard board;                           // MetaWearBoard class will help us to interact with the MetaWear board
    private TextView showTemp;                            // showTemp textview widget will be used to display the current temperature
    private Switch simpleSwitch;                          // Switch widget is being used to demonstrate the disabling of security at the doors during an emergency
    private AlertDialog alert;                            // AlertDialog has been used to alert the user about eruption of fire
    private AlertDialog.Builder builder;                  // builder for the alert dialog

    // Overriding onCreate method from the base class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        // attaching the TextView and Switch views present in the activity
        showTemp = (TextView) findViewById(R.id.TmpView);
        simpleSwitch = (Switch) findViewById(R.id.switch2);

        // creating and setting up the content and title of the Alert dialog box using AlertDialog.Builder class
        builder = new AlertDialog.Builder(this);
        builder.setMessage("Temperature is too HIGH. Door security disabled. Vacate the building!");
        alert = builder.create();
        alert.setTitle("FIRE ALERT!!!");
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFCB9B7")));


        Activity curractivity = (Activity) this;
        /**
         * Adding an onclickListener for the button to check temperature
         * when the click action is performed then the temperatureDataRead method is called
         */
        findViewById(R.id.checkTempButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // passing the reference of current activity to use inside the method temperatureDataRead
                temperatureDataRead(curractivity);
            }
        });

    }

    // onDestroy method is called when the activity is destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }
    // onServiceConnected method will get called when a connection to the service has been established
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("firealert", "Service connected");
        // method to connect to the MetaWear board, the mac address of the device is passed as a parameter
        retrieveBoard("E1:52:2F:81:EC:BC");
    }

    /**
     * this method establishes the bluetooth connection with the metawear
     * it takes a string as an input which contains the mac address of the Metawear device
     */
    private void retrieveBoard(String macAddr) {
        // Creating a manager object for the android bluetooth services
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // Creating a BluetoothDevice object using the BluetoothManager which connects to the MetaWear using the mac address contained in macAddr string
        final BluetoothDevice remoteDevice =
                btManager.getAdapter().getRemoteDevice(macAddr);

        // Creating a MetaWear board object for the Bluetooth Device to be used
        board = serviceBinder.getMetaWearBoard(remoteDevice);
        // try catch block to handle the exception, if thrown during the connection establishment phase
        try {
            // establishing the bluetooth LE connection with the MetaWear board using connectAsync method
            Task<Void> task = board.connectAsync();
            task.waitForCompletion();
            // the isFaulted method returns true if the task has an error, otherwise false
            // It helps to check whether the connection has been established or not
            if (task.isFaulted()) {
                Log.i("firealert", "failed to connect to the board");
            }
        }
        catch (InterruptedException e){}
        Log.i("firealert", "Connection Established with " + macAddr);

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
    /**
     * method setShowTemp takes the temperature value as an input in the form of
     * string and displays it on the textview
     */
    public void setShowTemp(String newtemp){
        // changing the text of the Textview showTemp to the new value
        showTemp.setText( newtemp );
    }
    /**
     * method setswitchAlert sets the Switch state to TRUE,
     * whenever the temperature crosses the safe value then this method is triggered,
     * this method demonstrates the switching of the emergency doors using the Switch widget,
     * in case of fire emergency the doors security is disabled
     */
    public void setswitchAlert(String insideIf){
        Log.i( "firealert", insideIf );
        // setting the state of Switch to true
        simpleSwitch.setChecked(true);
    }

    /**
     * Method to read temperature data from MetaWear Sensor and invoking appropriate methods according to the value read.
     */
    public void temperatureDataRead(Activity activity)
    {
        //Creating the temperature module object from the MetaWear board
        Temperature temperature = board.getModule(Temperature.class);
        // selecting the PRESET_THERMISTOR sensor from the sensors on the MetaWear Board,
        // here [0] is used to select the first PRESET_THERMISTOR sensor from the list
        final Temperature.Sensor tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];
        // Adding a route Asynchronously to the PRESET_THERMISTOR sensor
        tempSensor.addRouteAsync(new RouteBuilder() {
            @Override
            // Configuring the PRESET_THERMISTOR sensor
            public void configure(RouteComponent source) {
                // streaming the temperature data obtained by the sensors
                source.stream(new Subscriber() {
                    @Override
                    // Processing the streamed temperature sensor data
                    public void apply(Data data, Object... env) {
                        // logging the temperature value found
                        Log.i("firealert", "Temperature (C) = "+ data.value(Float.class).toString());
                        // storing the current date and time value to a string variable timeStamp
                        String timeStamp = new SimpleDateFormat("dd-M-yyyy hh:mm:ss").format(Calendar.getInstance().getTime());
                        Class[] args = new Class[1];
                        args[0] = String.class;
                        //using try and catch to handle exception while invoking method to display temperature
                        try {
                            Method settemp = activity.getClass().getMethod("setShowTemp",new Class[] { String.class });
                            try {
                                // passing the timestamp value and the data value in the form of string to the invoked method setShowTemp
                                settemp.invoke(activity, new String(timeStamp+ " Temperature = "+ data.value(Float.class).toString())+ " \u2103");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                        // logging if no exception is found
                        Log.i("firealert", "No exception found");
                        // handling the case when temperature crosses the safe level
                        if(data.value(Float.class)>21){

                            //using try and catch to handle exception while invoking method to toggle the door security switch
                            try {
                                Method switchAlert= activity.getClass().getMethod("setswitchAlert",new Class[]{ String.class});
                                try {
                                    // invoking the method setswitchAlert
                                    switchAlert.invoke(activity, new String("the doors are being closed"));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            // logging to ensure that no exception is found
                            Log.i("firealert", "No exeception found");
                            // Invoking alert using the runOnUiThread method as we are updating the UI from a non UI thread
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    // shows the alert dialog box to the user
                                    alert.show();
                                }
                            });
                        }
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                // method to read the data from the temperature sensor
                tempSensor.read();
                return null;
            }
        });

    }

}