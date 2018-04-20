#include "lightswitch.h"
#include "temp.h"
#include "lock.h"
#include "door.h"
//Third to 19, Last to 18
const int lightCommand = 1;
const int temperatureCommand = 2;
const int lockCommand = 3;
int ledPin = 22;
int tempPin = A1;
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
  Serial1.begin(9600);
  pinMode(ledPin, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial1.available()) {
    intent = (Serial1.readString());
    //Serial.println(intent);
    if (intent == "Greeting") {
      Serial1.println("Hello Mason!");
      Serial.println("Hello Mason!");
    } else if (intent == "Temperature") {
      myTemp.getCurrentTemp();
      androidMessage = myTemp.tempMessage();
      Serial1.println(androidMessage);
      Serial.println(androidMessage);
    } else if (intent == "Lights") {
      myLightSwitch.switchLights();
      androidMessage = myLightSwitch.lightMessage();
      Serial1.println(androidMessage);
      Serial.println(androidMessage);
    } else if (intent == "Door") {
      myDoor.switchDoor();
      androidMessage = myDoor.doorMessage();
      Serial1.println(androidMessage);
      Serial.println(androidMessage);
    } else {
      Serial1.println("Not supported");
      Serial.println("Not supported");
    }
  }
}
