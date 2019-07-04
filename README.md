# zvieratko
Remotetely-operated camera and sensors over wifi with NodeMCU 1.0 with ESP-12E

A simple project developed based on a specification from the client.

Architecture:

A wifi camera is observing a scene in the nature and transmitting its image to a distant observer sitting in a tent or a car (this part is not covered by this project, it is just a simple network wifi camera).

The PC with the view of the image from the camera also runs a java program, which is a tcp-server for communication with other nodes placed around the scene.

Near the camera, one (or more) units have the PIR sensors that are detecting the presence of the animal.
In that circumstance, they send a message to the PC, which notifies (wakes up) the observer => it emits a signal
through pin D2 of Arduino that is connected to the PC (a kind of buzzer).

Also near the camera, there is a remotely-operated photo-camera with another NodeMCU unit. When the observer pressed the button on his PC, a signal is emitted to the unit, and the camera shoots a picture.

Also near the camera, there is a remotely-operated sound emitter - that makes a certain sound that the animal will react to - such as raise its head, etc. This is also controlled from a button in the server GUI on the PC.

Similar to the sound, there is an alarm unit, that is used to scare away any unwanted intruder.

Finally, a sequence of sound followed by a camera shot can be initiated by another button in the simple server GUI.

Loss of wifi signal or server or individual nodes shutdown resumes automatically after the failure passes without any additional maintenance.

All NodeMCU units run the same code.


Usage:

1. Connect arduino to PC - check the serial port (such as COM3), and modify run.bat accordingly.
2. Setup the Wifi (and update the variables in the code as needed)
3. Connect the HW to the NodeMCU modules, leave them on
4. Connect the PC to the Wifi, and then run the server (run.bat)

If you make any changes in the java code, recompile with compile.bat.


Connections:

Arduino: 
  pin D2 -> wake-up buzzer
ESP units:
  pin D1 -> emit sound
  pin D2 -> emit alarm
  pin D3 -> shoot camera
  
  pin D6 <- PIR sensor signal 1
  pin D7 <- PIR sensor signal 2
  
  optional: 
    D5 -> piezo for debugging sounds
    D4 -> servo        
 
The setup assumes a fixed IP address of the PC.
It has to be configured directly in the esp.ino
(server_ip variable).
The SSID and PASSWORD for your wifi needs to be changed
both in the esp.ino and the ZvieratkoServer.java.


Included files:

 arduino/wakeup/wakeup.ino   - source for Arudino (e.g. Nano) with buzzer
 esp/esp.ino                 - source for all NodeMCU modules
 zvieratko/\*.java           - sources for Java server running on PC
 compile.bat                 - compile java sources
 run.bat                     - run PC server
 purejavacomm-1.0.3.jar      - PureJavaComm library (see dependencies)
 jna-4.0.0.jar               - JNA library (see dependencies) 
 
 
Requirements:

* JDK8
* PureJavaComm library, https://github.com/nyholku/purejavacomm
* PureJavaComm depents on JNA, https://github.com/java-native-access/jna
* Arduino IDE with ESP boards manager, use boards manager URL http://arduino.esp8266.com/stable/package_esp8266com_index.json

This software is public domain.
Developed in July 2019 by Pavel Petrovic, ppetrovic@acm.org.