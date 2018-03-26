# FINN :dog:
**F**airly **I**ntelligent **N**on-huma**N** is a personal assistant that is voice commanded through an your phone. It will be able to handle day to day requests ranging from inquiries (check the weather) to home automation (lock the doors). Using LUIS AI and Google Speech to text, the application on your phone will turn speech to text and send queries to LUIS which will send JSON back to the phone. The application on the phone will then parse the JSON and, depending on the task, will send it to the arduino for it to execute the commands (turn on/off the lights etc.). ~~Why buy an Alexa home instead? Because I can’t afford one and because I already have an arduino and free services from LUIS.~~

## Current Supported Features ##
### Lights ###
Currenty FINN is able to switch a LED light on or off depending on it's previous state.
### Temperature ###
Currently FINN is able to tell the temperature in the room when asked.
### Lock ###
Currently FINN is able to "lock or unlock" the door depending on it's previous state by using a servo motor.

## Circuit ##
![Arduino Circuit!](https://raw.githubusercontent.com/mkduan/FINN/master/Finn_Circuit.png)
*Be sure to use a lot of imagination!*

## What's next for FINN ##
I would like to expand it by adding more libraries so that I could handle even more tasks!
