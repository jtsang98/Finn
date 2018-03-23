#ifndef lightswitch_h
#define lightswitch_h

#include "Arduino.h"
class lightswitch {
  public:
  lightswitch(int ledPin);

  //Setters
  void switchLights();
  
  private:
  bool _lightsOn;
  int _ledPin;
};
#endif
