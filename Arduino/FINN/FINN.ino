#include <SoftwareSerial.h>
#include "lightswitch.h"
#include "temp.h"
#include "lock.h"
#include "door.h"

SoftwareSerial bluetooth(2,4); //RX, TX
const int lightCommand = 1;
const int temperatureCommand = 2;
const int lockCommand = 3;
int ledPin = 21;
int tempPin = 1;
int doorPin = 9;
bool lightsOn = false;
String intent;
String androidMessage = "";

lightswitch myLightSwitch(ledPin);
temp myTemp(tempPin);
door myDoor(doorPin);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  bluetooth.begin(9600);
  pinMode(ledPin, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(bluetooth.available()) {
    intent = (bluetooth.readString());
    //Serial.println(intent);
    //TODO: Make each class change the message back to Android
    //TODO: Android parse message and look for key words at the end
    if (intent == "Greeting") {
      bluetooth.println("Hello Mason!");
      Serial.println("Hello Mason!");
    } else if (intent == "Temperature") {
      myTemp.getCurrentTemp();
      androidMessage = myTemp.tempMessage();
      bluetooth.println(androidMessage);
      Serial.println(androidMessage);
    } else if (intent == "Lights") {
      myLightSwitch.switchLights();
      androidMessage = myLightSwitch.lightMessage();
      bluetooth.println(androidMessage);
      Serial.println(androidMessage);
    } else if (intent == "Door") {
      myDoor.switchDoor();
      androidMessage = myDoor.doorMessage();
      bluetooth.println(androidMessage);
      Serial.println(androidMessage);
    } else {
      bluetooth.println("Not supported");
      Serial.println("Not supported");
    }
  }
}
