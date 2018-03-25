#include "Arduino.h"
#include "temp.h"

temp::temp(int tempPin) {
  _tempPin = tempPin;
}

void temp::getCurrentTemp() {
  float volt = analogRead(_tempPin)*5.0/1024.0;
  _tempC = (volt - 0.5)*100.0;
}

String temp::tempMessage() {
  //TODO: See if the period messes stuff up.
  String message = beginning + _tempC + " degrees celsius.";
  return message;
}
