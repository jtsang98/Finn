#ifndef temp_h
#define temp_h

#include "Arduino.h"
class temp {
public:
  temp(int tempPin);

  //Getter
  void getCurrentTemp();

private:
  int _tempPin;
  int _tempC;
};
#endif
