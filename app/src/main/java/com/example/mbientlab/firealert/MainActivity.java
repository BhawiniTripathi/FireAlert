package com.example.mbientlab.firealert;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.mbientlab.metawear.CodeBlock;
import com.mbientlab.metawear.module.Gpio;

import com.mbientlab.metawear.module.Temperature;
import com.mbientlab.metawear.module.Temperature.SensorType;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.module.Timer;

import bolts.Continuation;
import bolts.Task;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private BtleService.LocalBinder serviceBinder;
    private MetaWearBoard board;
    // final Temperature temperature = board.getModule(Temperature.class);
    // final Temperature.Sensor tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];
    private Temperature temperature;
    private Temperature.Sensor tempSensor;
    TextView textView;
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        /*TextView*/ textView = (TextView) findViewById(R.id.textView);
        //textView.setText(data.value(Float.class).toString());
        /*findViewById(R.id.start).setOnClickListener(new View.OnClickListener(){

             @Override
            public void onClick(View v) {
             tempSensor.start();

            }
        });
              /*  findViewById(R.id.stop).setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        tempSensor.stop();

                    }
                });*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (BtleService.LocalBinder) service;
        Log.i("firealert", "Service Connected");
        retrieveBoard("E1:52:2F:81:EC:BC");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
    private void retrieveBoard(String macAddr) {
        final BluetoothManager btManager=
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice=
                btManager.getAdapter().getRemoteDevice(macAddr);

        // Create a MetaWear board object for the Bluetooth Device
        board= serviceBinder.getMetaWearBoard(remoteDevice);
        board.connectAsync().continueWith(new Continuation<Void, Void>(){

            @Override
            public Void then(Task<Void> task) throws Exception {

                Log.i("firealert", "Connected to" + macAddr);
                temperature = board.getModule(Temperature.class);
                tempSensor = temperature.findSensors(SensorType.PRESET_THERMISTOR)[0];

                tempSensor.addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {

                        source.stream(new Subscriber() {
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("firealert", "Temperature (C) = " + data.value(Float.class).toString());
                                // TextView textView = (TextView) findViewById(R.id.textView);
                                textView.setText(data.value(Float.class).toString());
                            }
                        });

                    }
                });
                //@Override
                //public Void then(Task<Route> task) throws Exception {
                       /* if (task.isFaulted()) {
                            Log.w("firealert", "failed to configure app", task.getError());
                        } else {
                            Log.i("firealert", "App Configured");
                        }*/
                //while(true) {

                tempSensor.read();


                return null;
                //}
            }
            /*public Task<Timer.ScheduledTask> scheduleRead(final Temperature.Sensor tempSensor) {
                // send a read command to the dadta producer every 30 seconds, start immediately
                return timer.scheduleAsync(30000, false, new CodeBlock() {
                    @Override
                    public void program() {

                    }tempSensor.read();
                });
            }*/
            /*.continueWith(new Continuation<Route, Void>() {
            @Override
            public Void then(Task<Route> task) throws Exception {
                if (task.isFaulted()) {
                    Log.w("firealert", "failed to configure app", task.getError());
                } else{
                    Log.i("firealert", "App Configure");
                }
                //while(true) {
                tempSensor.read();
                //}
                return null;
            }
        });*/

        });

    }}