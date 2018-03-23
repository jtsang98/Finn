#include "Arduino.h"
#include "lock.h"

lock::lock(int lockPin) {
  _lockPin = lockPin;
  _lockOn = false;
}
