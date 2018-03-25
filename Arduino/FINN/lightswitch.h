#ifndef lightswitch_h
#define lightswitch_h

#include "Arduino.h"
class lightswitch {
  public:
  lightswitch(int ledPin);

  //Setters
  void switchLights();

  //Getter
  String lightMessage();

  private:
  bool _lightsOn;
  int _ledPin;
  String beginning = "The light is ";
};
#endif
