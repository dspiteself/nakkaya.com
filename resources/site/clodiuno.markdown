---
title: Clodiuno - Clojure API for Arduino
description: Clodiuno is a Clojure API for Arduino.
tags: clojure clodiuno arduino
flatter: yes
---

Clodiuno is a library that allows you to control Arduino using Clojure
allowing Clojure developers to interface with real world using Arduino
hardware.

Currently Clodiuno supports two interfaces, you can either use the USB
connection via Firmata or you can connect to your Arduino using WiFi via
WiShield.

#### Dependencies (Firmata)

Firmata is a serial binary protocol, to get Java communicate via Serial
Port there are two options,
[JavaComm](http://java.sun.com/products/javacomm/) API and
[rxtx](http://users.frii.com/jarvi/rxtx/) API. Clodiuno uses rxtx,
because thats what Arduino IDE uses, you already have the Jars required
installed with your Arduino installation.

From your Arduino installation copy,

 - RXTXcomm.jar
 - librxtxSerial.jnilib (OS X Users)
 - librxtxSerial.dll (Windows Users)
 - librxtxSerial.so (Linux Users)

into your Java extensions folder, if you choose to place the Jars
somewhere else make sure to set your Java library path to point to that
folder too.

For Mac OS X users they are located inside the application bundle,

    open /Applications/Arduino.app/Contents/Resources/Java/

Also if you are on Mac OS X, make sure you use 32 bit Java 1.5.0,
otherwise RXTX won't work.

#### Dependencies (WiShield)

None.

#### Installation

Clodiuno is also available via [Clojars](http://clojars.org/clodiuno),
for now only Mac OS X native dependencies are available on
clojars.

     (defproject arduino-project "1.0.0-SNAPSHOT"
       :dependencies [[org.clojure/clojure "1.1.0"]
                      [org.clojure/clojure-contrib "1.1.0"]
                      [clodiuno "0.0.2-SNAPSHOT"]]
       :native-dependencies [[org.clojars.nakkaya/rxtx-macosx-native-deps "2.1.7"]]
       :dev-dependencies [[native-deps "1.0.5"]])

#### For Firmata Interface

You need to upload Firmata sketch to your Arduino, 

    File -> Examples -> Firmata -> StandartFirmata

#### For WiShield Interface

Make sure
[WiShield](http://asynclabs.com/wiki/index.php?title=AsyncLabsWiki)
library is configured to compile and run
[SocketApp](http://asynclabs.com/wiki/index.php?title=SocketApp_sketch)
sketch, once configured you can upload the wishield sketch located in
the resources folder.

#### Usage

resources/examples/ folder contains  examples to give a feel for the
API, I also have the following hacks using Clodiuno,

 - [Blinking SOS](/2010/01/03/clodiuno-a-clojure-api-for-the-firmata-protocol/)
 - [Servo Control](/2010/01/06/making-things-move-with-clojure/)
 - [Etch A Sketch](/2010/02/02/etch-a-sketch/)
 - [ESC Control](/2010/05/21/motor-control-via-esc-using-arduino-and-clodiuno/)
 - [Ardumoto Control](/2010/06/04/motor-control-via-ardumoto-using-arduino-and-clodiuno/)
 - [Using WiShield and Firmata Together](/2010/08/09/wishield-support-for-clodiuno/)
 - [Clodiuno/Processing ADXL335 Accelerometer](/2010/09/28/clodiuno-processing-adxl335-accelerometer/)
 - [Duck Hunt Experiment](/2011/01/04/duck-hunt-experiment/)

#### Source

Project is hosted at github, grab it
[here](http://github.com/nakkaya/clodiuno).

#### License

Beerware Revision 42
