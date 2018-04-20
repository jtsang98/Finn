package com.example.mason.finn;

import android.util.Log;

/**
 * Created by mason on 2018-04-19.
 */


public class ResponseHandler {
    public ResponseHandler(){

    }
    public String responseMessage(String arduinoReply) {
        Log.d("ArduinoReply", "ArduinoReply: " + arduinoReply);
        String response;
        String[] tokens = arduinoReply.split(" ");
        String prefix = tokens[1];
        Log.d("prefix", "responseMessage: " + prefix);
        //Same value but not the same object
        if (prefix.equals("temperature")){
            String value = tokens[3];
            response = "The temperature is " + value + " degrees celsius.";
        } else if (prefix.equals("light")){
            String value = tokens[3];
            response = "The light is " + value;
        }else if (prefix.equals("Door")) {
            String value = tokens[3];
            response = "The door is "+ value;
        } else if (tokens[0].equals("Hello")){
            //Token 2 can be specified for name
            response = "Hello Mason!";
        }else{
            response = "Not Supported";
        }

        return response;
    }
}
