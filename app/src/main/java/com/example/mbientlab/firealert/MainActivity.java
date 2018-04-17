package com.example.mbientlab.firealert;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;
    static MetaWearBoard board;
    private TextView showTemp;
    private Switch simpleSwitch;
    private AlertDialog alert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);

        showTemp = (TextView) findViewById(R.id.TmpView);
        simpleSwitch = (Switch) findViewById(R.id.switch2);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Temperature Is Too HIGH");
        alert = builder.create();
        alert.setTitle("ALERT!!!!!!!!");

        findViewById(R.id.checkTempButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //func();
                temperatureDataRead();
            }


        });
        // temperatureDataRead();
    }

    /*private void func() {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        temperatureDataRead();
                    }
                },
                3000
        );
    }*/

      /*  private void func() {
            while(true){
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                temperatureDataRead();
            }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("firealert", "Service connected");
        retrieveBoard("E1:52:2F:81:EC:BC");
    }

    private void retrieveBoard(String macAddr) {
        final BluetoothManager btManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice =
                btManager.getAdapter().getRemoteDevice(macAddr);

        // Create a MetaWear board object for the Bluetooth Device
        board = serviceBinder.getMetaWearBoard(remoteDevice);
        try {
            Task<Void> task = board.connectAsync();
            task.waitForCompletion();

            if (task.isFaulted()) {
                Log.i("firealert", "failed to connect to the board");
            }
        }
        catch (InterruptedException e){}
        Log.i("firealert", "Connection Established with " + macAddr);

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }

    public void temperatureDataRead()
    {

        Temperature temperature = board.getModule(Temperature.class);
        final Temperature.Sensor tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];
        tempSensor.addRouteAsync(new RouteBuilder() {
            @Override
            public void configure(RouteComponent source) {
                source.stream(new Subscriber() {
                    @Override
                    public void apply(Data data, Object... env) {
                        // TextView showTemp = (TextView) findViewById(R.id.TmpView);
                        //String x = "Temperature (C) = " + data.value(Float.class);
                        Log.i("firealert", "Temperature (C) = "+ data.value(Float.class).toString());
                        showTemp.setText( "Temperature(In degree Celsius) = "+ data.value(Float.class).toString());
                        if(data.value(Float.class)>24){

                            //alert.show();
                            Log.i("firealert", "Alert!");
                            //simpleSwitch.setChecked(true);

                        }
                    }
                });
            }
        }).continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                tempSensor.read();
                return null;
            }
        });;

    }

}