package com.example.mason.finn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;


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

    private Handler mHandler;
    private ConnectedThread mConnectedThread;

    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mReadBuffer = (TextView)findViewById(R.id.readBuffer);
        mscanButton = (Button)findViewById(R.id.scan);
        moffButton = (Button)findViewById(R.id.off);
        mdiscoverButton = (Button)findViewById(R.id.discover);
        mlistPairedDevicesButton = (Button)findViewById(R.id.PairedBtn);
        mLed = (CheckBox)findViewById(R.id.checkboxLED1);

        mBluetoothArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mDeviceList = (ListView)findViewById(R.id.devicesListView);
        mDeviceList.setAdapter(mBluetoothArrayAdapter);
        mDeviceList.setOnItemClickListener(mDeviceClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = new Handler() {
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
        };
        //If there is not listed devices
        if (mBluetoothAdapter == null) {
            mbluetoothStatus.setText("Status: Bluetooth not Supported");
        } else {
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
        }
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

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        // Check which request we're responding to
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
}

