#ifndef lock_h
#define lock_h

#include "Arduino.h"
class lock {
public:
  lock(int lockPin);

  //Setter
  void switchLock();

private:
  int _lockPin;
  bool _lockOn;
};
#endif
