#include "Arduino.h"
#include <Servo.h>
#include "door.h"

door::door(int doorPin) {
  _doorPin = doorPin;
  _doorLocked = false;
  _position = 0;
  _doorLock.attach(9);
}

void door::switchDoor() {
  if (_doorLocked) {
    while (_position != 0) {
      this->unlockTheDoor();
    }
    _doorLocked = !_doorLocked;
  } else {
    while (_position != 90) {
      this->lockTheDoor();
    }
    _doorLocked = !_doorLocked;
  }
}

void door::lockTheDoor() {
  for (_position; _position <= 90; _position += 1) {
    _doorLock.write(_position);
    // wait 15 ms for servo to reach the position
    delay(15);
  }
}

void door::unlockTheDoor() {
  for (_position; _position >= 0; _position -= 1) {
    _doorLock.write(_position);
    // wait 15 ms for servo to reach the position
    delay(15);
  }
}

String door::doorMessage() {
  String message;
  if (_doorLocked) {
    //TODO:: Check if the period messes it up.
    message = beginning + "locked.";
  } else {
    message = beginning + "unlocked.";
  }
  return message;
}
