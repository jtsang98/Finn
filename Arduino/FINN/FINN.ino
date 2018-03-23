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

char a;
void loop() {
  // put your main code here, to run repeatedly:
  if(bluetooth.available()) {
    a = (bluetooth.read());
    if (a == '1') {
      bluetooth.println("Yeah you do!");
      Serial.println("I hate myself!");
    }
  }
}
