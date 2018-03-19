package com.example.mason.finn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private TextView mbluetoothStatus;
    private TextView mReadBuffer;
    private Button mscanButton;
    private Button moffButton;
    private Button mdiscoverButton;
    private Button mlistPairedDevicesButton;
    private CheckBox mLed;
    private ListView mDeviceList;

    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mBluetoothArrayAdapter;

    private TextView textSpeechInput;
    private ImageButton speakButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //maybe private?
    public BluetoothSocket mbluetoothSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    private Handler mHandler;
    private ConnectedThread mConnectedThread;

    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*mbluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView)findViewById(R.id.readBuffer);
        mscanButton = (Button)findViewById(R.id.scan);
        moffButton = (Button)findViewById(R.id.off);
        mdiscoverButton = (Button)findViewById(R.id.discover);
        mlistPairedDevicesButton = (Button)findViewById(R.id.PairedBtn);
        mLed = (CheckBox)findViewById(R.id.checkboxLED1);*/

        textSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        speakButton = (ImageButton) findViewById(R.id.btnSpeak);

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechInput();
            }
        });

        mBluetoothArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mDeviceList = (ListView)findViewById(R.id.devicesListView);
        mDeviceList.setAdapter(mBluetoothArrayAdapter);
        //TODO: Onclick listener to connect to FINN
        mDeviceList.setOnItemClickListener(mDeviceClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /*mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }
                if(msg.what == CONNECTING_STATUS) {
                    if(msg.arg1 == 1){
                        mbluetoothStatus.setText("Connected to Device: " + (String)(msg.obj));
                    }else {
                        mbluetoothStatus.setText("Connection Failed");
                    }
                }
            }
        };*/

        //If there is not listed devices
        if (mBluetoothAdapter == null) {
            //mbluetoothStatus.setText("Status: Bluetooth not Supported");
            Toast.makeText(getApplicationContext(), "Bluetooth not Supported", Toast.LENGTH_SHORT).show();
        } else {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if(mBluetoothAdapter.isEnabled()) {
                for(BluetoothDevice device: mPairedDevices) {
                    mBluetoothArrayAdapter.add(device.getName() + "\n" +device.getAddress());
                }
            } else {
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
            /*
            mLed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mConnectedThread != null){
                        mConnectedThread.write("1");
                    }
                }
            });

            mscanButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    bluetoothOn(view);
                }
            });

            moffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bluetoothOff(view);
                }
            });

            mlistPairedDevicesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listPairedDevices(view);
                }
            });
            mdiscoverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    discover(view);
                }
            });
        }*/
    }

    private void bluetoothOn(View view) {
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth,REQUEST_ENABLE_BT);
            mbluetoothStatus.setText("Bluetooth enabled");
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // TODO: don't really need the onActivityResult to handle UI bluetooth
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mbluetoothStatus.setText("Enabled");
            }
            else
                mbluetoothStatus.setText("Disabled");
        }

        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if(resultCode == RESULT_OK && Data != null) {
                    ArrayList<String> result = Data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String mySpeech = result.get(0);
                    Toast.makeText(getApplicationContext(), mySpeech, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void bluetoothOff(View view) {
        mBluetoothAdapter.disable();
        mbluetoothStatus.setText("Bluetooth disabled");
    }

    private void listPairedDevices(View view) {
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        if(mBluetoothAdapter.isEnabled()) {
            for(BluetoothDevice device: mPairedDevices) {
                mBluetoothArrayAdapter.add(device.getName() + "\n" +device.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
        }
    }

    private void discover(View view) {
        //If already discovering cancel it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "Discovery Cancelled", Toast.LENGTH_SHORT).show();
        } else{
            if(mBluetoothAdapter.isEnabled()) {
                mBluetoothArrayAdapter.clear();
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "Discovery Started", Toast.LENGTH_SHORT).show();
                registerReceiver(bluetoothReciever,new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver bluetoothReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Add the device name to the list of bluetooth devices
                mBluetoothArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBluetoothArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth isn't on", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                String info = ((TextView) view).getText().toString();
                Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                final String address = info.substring(info.length() - 17);
                final String name = info.substring(0, info.length() - 17);

                new Thread() {
                    public void run() {
                        boolean fail = false;

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                        try{
                            mbluetoothSocket =createBluetoothSocket(device);
                        } catch (IOException e) {
                            e.printStackTrace();
                            fail = true;
                            Toast.makeText(getApplicationContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }

                        try{
                            mbluetoothSocket.connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                            fail = true;
                            try {
                                mbluetoothSocket.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                Toast.makeText(getApplicationContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(fail == false){
                            mConnectedThread = new ConnectedThread(mbluetoothSocket);
                            mConnectedThread.start();
                            Toast.makeText(getApplicationContext(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }.start();
            }
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;

            InputStream tempIn = null;
            OutputStream tempOut = null;

            //Get the input and output streams, using temp objects because member streams are final.
            try{
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tempIn;
            mmOutStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            //Keep listening to inputStream until an exception happens
            while(true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes!=0) {
                        SystemClock.sleep(100); //Waits for the data

                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);

                        mHandler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        //Send data to the Arduino
        public void write(String input) {
            byte[] bytes = input.getBytes(); //String to bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // shut down the connection
        public void cancel() {
            try{
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void speechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.greeting));
        try{
            startActivityForResult(intent,REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Speech is not supported", Toast.LENGTH_SHORT).show();
        }
    }
}

//TODO: After get arduino, mkae address and make is to that uses voice to connect to Finn's address