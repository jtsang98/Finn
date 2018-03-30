package com.example.mason.finn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by mason on 2018-03-23.
 */


public class MainActivity extends AppCompatActivity {

    private ListView mDeviceList;
    private LinearLayout speechLayout;
    private TextToSpeech FINN;
    private String query;
    private String mIntent;

    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mBluetoothArrayAdapter;

    private TextView textSpeechInput;
    private ImageButton speakButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //maybe private?
    public BluetoothSocket mBluetoothSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    private Handler mHandler;
    private ConnectedThread mConnectedThread;
    
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        speakButton = (ImageButton) findViewById(R.id.btnSpeak);

        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechInput();
            }
        });

        speechLayout = (LinearLayout)findViewById(R.id.speech);

        mBluetoothArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mDeviceList = (ListView)findViewById(R.id.devicesListView);
        mDeviceList.setAdapter(mBluetoothArrayAdapter);
        //TODO: Onclick listener to connect to FINN
        mDeviceList.setOnItemClickListener(mDeviceClickListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        FINN = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    FINN.setLanguage(Locale.UK);
                }
            }
        });


        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //TODO: Speech to text
                    Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT).show();
                    FINN.speak(readMessage, TextToSpeech.QUEUE_FLUSH,null);
                }
                if(msg.what == CONNECTING_STATUS) {
                    if(msg.arg1 == 1){
                        Toast.makeText(getApplicationContext(), "Connected to Device" + (String)(msg.obj), Toast.LENGTH_SHORT).show();
                        mDeviceList.setVisibility(View.GONE);
                        speechLayout.setVisibility(View.VISIBLE);
                    }else {
                        Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data){
        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if(resultCode == RESULT_OK && Data != null) {
                    ArrayList<String> result = Data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    query = result.get(0);
                    Toast.makeText(getApplicationContext(), query, Toast.LENGTH_SHORT).show();
                    getIntent requestIntent = new getIntent();
                    try {
                        requestIntent.execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if(mConnectedThread != null){
                        Toast.makeText(getApplicationContext(), "Send to Arduino", Toast.LENGTH_SHORT).show();
                        mConnectedThread.write(mIntent);
                    }
                }
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                    try {
                        mBluetoothSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBluetoothSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBluetoothSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(fail == false) {
                        mConnectedThread = new ConnectedThread(mBluetoothSocket);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();

                    }
                }
            }.start();
        }
    };
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
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

    private class getIntent extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),"Json Data is downloading",Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String url = queryBuilder(query);
            Log.d("MainActivity", "URL: " + url);
            String jsonStr = sh.makeServiceCall(url);
            Log.d("MainActivity", "Response from url: " + jsonStr);

            if(jsonStr != null) {
                try{
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject topScoringIntent = jsonObj.getJSONObject("topScoringIntent");
                    mIntent = topScoringIntent.getString("intent");
                    Log.d("MainActivity", mIntent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private String queryBuilder(String query) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/64ca82ad-863e-4d6a-bdf5-d51e08e31b47?subscription-key=bb68dd52bfe146819547d21e95798279&verbose=true&timezoneOffset=0&q=");
            String[] tokens = query.split(" ");
            for (String t:tokens) {
                stringBuilder.append(t);
                stringBuilder.append("%20");
            }
            return stringBuilder.toString();
        }
    }
}

//TODO: After get arduino, mkae address and make is to that uses voice to connect to Finn's address