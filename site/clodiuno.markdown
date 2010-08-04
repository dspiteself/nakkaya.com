---
title: Clodiuno - Firmata API for Clojure
description: Clodiuno is a Clojure API for the firmata protocol.
tags: clojure clodiuno arduino
---


Clodiuno is a library that allows you to control Arduino via Firmata
protocol. The purpose of this library is to allow Clojure developers to
interface with real world using Arduino hardware.

#### Depencies

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

Clodiuno is also available via [Clojars](http://clojars.org/clodiuno),
for now only Mac OS X native dependencies are available on
clojars.

     (defproject arduino-project "1.0.0-SNAPSHOT"
       :dependencies [[org.clojure/clojure "1.1.0"]
                      [org.clojure/clojure-contrib "1.1.0"]
                      [clodiuno "0.0.1-SNAPSHOT"]]
       :native-dependencies [[org.clojars.nakkaya/rxtx-macosx-native-deps "2.1.7"]]
       :dev-dependencies [[native-deps "1.0.0"]])
  

#### Souce

Project is hosted at github, grab it
[here](http://github.com/nakkaya/clodiuno).

#### Installation

You need to upload Firmata sketch to your Arduino, 

    File -> Examples -> Firmata -> StandartFirmata

But Firmata sketch shipped with Arduino v17 is buggy, for clodiuno to
work you need to [download](http://www.firmata.org/wiki/Download) latest
sketch and use it instead of the one supplied with arduino.

You can either put the src/ folder on to your classpath, or copy
clodiuno.clj into your project. For now API is contained in a single
file.

#### Usage

examples/ folder contains two simple examples to give a feel for the
API, I also have the following hacks using Clodiuno,

 - [Blinking SOS](/2010/01/03/clodiuno-a-clojure-api-for-the-firmata-protocol/)
 - [Servo Control](/2010/01/06/making-things-move-with-clojure/)
 - [Etch A Sketch](/2010/02/02/etch-a-sketch/)
 - [ESC Control](/2010/05/21/motor-control-via-esc-using-arduino-and-clodiuno/)
 - [Ardumoto Control](/2010/06/04/motor-control-via-ardumoto-using-arduino-and-clodiuno/)

#### License

Beerware Revision 42

<script type="text/javascript">
	var flattr_url = 'http://nakkaya.com/clodiuno.markdown';
</script>
<script src="http://api.flattr.com/button/load.js" type="text/javascript"></script>
<br>
