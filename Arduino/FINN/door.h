#ifndef door_h
#define door_h

#include "Arduino.h"
#include <Servo.h>
class door {
public:
  door(int doorPin);

  //Setter
  void switchDoor();
  void lockTheDoor();
  void unlockTheDoor();

  //Getter
  String doorMessage();

private:
  int _doorPin;
  bool _doorLocked;
  String beginning = "The door is ";
  Servo _doorLock;
  int _position;
};
#endif
