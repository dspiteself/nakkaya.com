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

#### Souce

Project is hosted at github, grab it
[here](http://github.com/nakkaya/clodiuno).

#### Installation

You can either put the src/ folder on to your classpath, or copy
clodiuno.clj into your project. For now API is contained in a single
file.

#### Usage

examples/ folder contains two simple examples to give a feel for the
API, I also have the following hacks using Clodiuno,

 - [Blinking SOS](/2010/01/03/clodiuno-a-clojure-api-for-the-firmata-protocol/)
 - [Servo Control](/2010/01/06/making-things-move-with-clojure/)

#### License

Beerware Revision 42
