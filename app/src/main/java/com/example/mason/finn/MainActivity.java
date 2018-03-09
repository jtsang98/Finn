package com.example.mason.finn;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    private TextView mbluetoothStatus;
    private TextView mReadBuffer;
    private Button mscanButton;
    private Button moffButton;
    private Button mdiscoverButton;
    private Button mlistPairedDevicesButton;
    private CheckBox mLed;
    private ListView mDeviceList;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mBluetoothArrayAdapter;

    private Handler mHandler;

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
    }
}
