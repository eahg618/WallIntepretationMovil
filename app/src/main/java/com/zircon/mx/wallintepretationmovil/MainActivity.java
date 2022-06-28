package com.zircon.mx.wallintepretationmovil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {

    Button btnOn, btnOff;
    TextView txtArduino, txtDevicePosition, txtResponseFromDevice, sensorView0, sensorView1, sensorView2, sensorView3;
    TextView txtSendorLDR;
    Handler bluetoothIn;

    final int handlerState = 0;                         //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;
    private AreaDibujo areaDibujo;
    private List<Pair<Integer, Integer>> movimientos;
    private int posX;
    private int posY;
    private boolean movimientoUp;
    private boolean movimientoDown;
    private boolean movimientoLeft;
    private boolean movimientoRight;
    private boolean PararEnvio;
    private Timer timer;
    private TimerTask task;


    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        areaDibujo = (AreaDibujo) findViewById(R.id.Lienzo);
        movimientos = new ArrayList<>();
        posX = 10;
        posY = 700;
        movimientoUp = false;
        movimientoDown = false;
        movimientoLeft = false;
        movimientoRight = false;
        timer = new Timer();


        //Link the buttons and textViews to respective views
        this.btnOn = (Button) findViewById(R.id.buttonOn);
        this.txtDevicePosition = (TextView) findViewById(R.id.txtString);
        this.txtResponseFromDevice = (TextView) findViewById(R.id.ResponseFromDevice);
        txtSendorLDR = (TextView) findViewById(R.id.tv_sendorldr);


        //handler para recivir la informacion en el activity y desplegarla
        bluetoothIn = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == handlerState) {
                    Integer readMessage = (Integer) msg.obj;        //mensaje enviado despues de tratar la informacion del buffer
                    if (readMessage > 126) {

                        posY=400;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            areaDibujo.ColocarPunto(posX, posY);
                        }
                    } else {
                        posY = 700;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            areaDibujo.ColocarPunto(posX, posY);
                        }
                    }
                    //txtResponseFromDevice.setText(readMessage);

                }
            }
            //}
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // obtiene Bluetooth adapter
        checkBTState();

        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // mConnectedThread.write("BATV00000000AD");    // envia comando via Bluetooth
                // mConnectedThread.write("AMPL00000000AA");
            }
        });

          task = new TimerTask() {
            @Override
            public void run() {
                mConnectedThread.write("AMPL00000000AA");
            }
        };

    }

    //implementar thread. Se pasa como parametro al thread : new Thread('Nombre del objeto thread').start()
    public class MessageToDevice implements Runnable {

        @Override
        public void run() {
            Log.d("MessageToDevice", "En MessageToDevice");
            try {
                Thread.sleep(1000);
                mConnectedThread.write("AMPL00000000AA");    // envia comando via Bluetooth
            } catch (Exception ex) {
                Log.d("MessageToDevice", "En MessageToDevice");
            }
        }
    }


    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {


        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
        if (sensors.size() > 0) {
            sm.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS.toString());

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {

            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
       timer.schedule(task, 10, 200); //start timer, request AMPL
        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this, sensorAccelerometer);
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @Override
    public void onStop() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.unregisterListener(this, sensorAccelerometer);

        super.onStop();
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    @SuppressLint("MissingPermission")
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        movimientos.add(new Pair(Math.round(sensorEvent.values[0]), Math.round(sensorEvent.values[1])));
        // Log.d("Posiciones", "X:" + Math.round(sensorEvent.values[0]) + " Y:" + Math.round(sensorEvent.values[1]));
        //MOVIMIENTOS PARA LA X
      /*  if (Math.round(sensorEvent.values[0]) > 0 && movimientoLeft == false)//movimiento a la derecha
        {
            posX = posX + 1;
            movimientoRight = true;
            areaDibujo.ColocarPunto(posX, posY);

        } else if (Math.round(sensorEvent.values[0]) < 0 && movimientoRight == false)//movimiento a la Izq
        {
            posX = posX - 1;
            movimientoLeft = true;
            areaDibujo.ColocarPunto(posX, posY);
        }
        else if (Math.round(sensorEvent.values[0]) == 0)
        {
            movimientoRight=false;
            movimientoLeft =false;
        }*/


       /* //MOVIMIENTOS PARA LA Y
        if (Math.round(sensorEvent.values[1]) > 0 && movimientoDown == false)//movimiento a la arriba
        {
            posY = posY + 1;
            movimientoUp = true;
            areaDibujo.ColocarPunto(posX, posY);
        } else if (Math.round(sensorEvent.values[1]) < 0 && movimientoUp == false)//movimiento a la abajo
        {
            posY = posY - 1;
            movimientoDown = true;
            areaDibujo.ColocarPunto(posX, posY);
        } else if (Math.round(sensorEvent.values[1]) == 0) {
            movimientoUp = false;
            movimientoDown = false;
        }*/

        if (posX == 1000) {
            areaDibujo.posiciones.clear();
            posX = 0;
            posY = 700;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            areaDibujo.ColocarPunto((posX = posX + 1), posY);
        }
        this.txtDevicePosition.setText("X:" + Math.round(sensorEvent.values[0]) + " Y:" + Math.round(sensorEvent.values[1]) + " Z:" + Math.round(sensorEvent.values[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //Keep looping to listen for received messages
        public void run() {
            byte[] buffer = new byte[14];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);   //read bytes from input buffer
                    String messageNoConvertion = "";

                    for (int i = 0; i < bytes; i++) {
                        messageNoConvertion += String.valueOf(buffer[i]);
                    }

                    String readMessage = new String(buffer, 0, bytes);
                    Log.d("Mensajes", readMessage);

                    //convertir el valor a decimal
                    String convertedStringToHEX = "";
                    for (int i = 0; i < messageNoConvertion.length(); i++) {
                        String substring = messageNoConvertion.substring(i, i + 2);
                        int a = Integer.parseInt(substring);
                        convertedStringToHEX += Integer.toHexString(a);
                        i = i + 1;
                    }
                    String realMessage = this.hexToAscii(convertedStringToHEX);
                    int messageDecimalAmplitud=0;
                    if (realMessage.contains("CHK") == false) {
                        messageDecimalAmplitud= Integer.parseInt(realMessage.substring(4), 16);
                        Log.d("Mensajes", String.valueOf(messageDecimalAmplitud));
                    }
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, messageDecimalAmplitud).sendToTarget();// Send the obtained bytes to the UI Activity via handler

                } catch (Exception e) {
                    break;
                }
            }
        }

        private String hexToAscii(String hexStr) {
            StringBuilder output = new StringBuilder("");
            for (int i = 0; i < hexStr.length(); i += 2) {
                String str = hexStr.substring(i, i + 2);
                output.append((char) Integer.parseInt(str, 16));
            }
            return output.toString();
        }

        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);//write bytes over BT connection via outstream

            } catch (Exception e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }


}

