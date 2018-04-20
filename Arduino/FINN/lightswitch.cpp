#include "Arduino.h"
#include "lightswitch.h"

lightswitch::lightswitch(int ledPin) {
  _ledPin = ledPin;
  _lightsOn = false;
}

void lightswitch::switchLights() {
  if (_lightsOn) {
    digitalWrite(_ledPin, LOW);
    _lightsOn = !_lightsOn;
  } else {
    digitalWrite(_ledPin, HIGH);
    _lightsOn = !_lightsOn;
  }
}

String lightswitch::lightMessage() {
  String message;
  if (_lightsOn) {
    //TODO:: Check if the period messes it up.
    message = beginning + "on. ";
  } else {
    message = beginning + "off. ";
  }
  return message;
}
