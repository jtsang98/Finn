#ifndef temp_h
#define temp_h

#include "Arduino.h"
class temp {
public:
  temp(int tempPin);

  //Setter
  void getCurrentTemp();

  //Getter
  String tempMessage();

private:
  int _tempPin;
  float _tempC;
  String beginning = "The temperature is ";
};
#endif
