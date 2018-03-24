#include <SoftwareSerial.h>
#include "lightswitch.h"
#include "temp.h"
#include "lock.h"

SoftwareSerial bluetooth(2,4); //RX, TX
const int lightCommand = 1;
const int temperatureCommand = 2;
const int lockCommand = 3;
int ledPin = 21;
int tempPin = 1;
bool lightsOn = false;

lightswitch myLightSwitch(ledPin);
temp myTemp(tempPin);

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  bluetooth.begin(9600);
  pinMode(ledPin, OUTPUT);
}

String intent;
void loop() {
  // put your main code here, to run repeatedly:
  if(bluetooth.available()) {
    intent = (bluetooth.readString());
    Serial.println(intent);
    if (intent == "Temperature") {
      bluetooth.println("The temperature is ");
      Serial.println("The temperature is ");
    } else if (intent == "Lights") {
      bluetooth.println("The lights are ");
      Serial.println("The lights are ");
    } else if (intent == "Door") {
      bluetooth.println("The door is ");
      Serial.println("The door is ");
    } else {
      bluetooth.println("Not supported");
      Serial.println("Not supported");
    }
  }
}
