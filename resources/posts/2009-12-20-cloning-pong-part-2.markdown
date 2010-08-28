---
title: Cloning Pong Part 2
tags: clojure arduino pong
---

This write up is part two of cloning pong, we will modify the pong game
we created in [part one](/2009/12/19/cloning-pong-part-1/), to be able
to control it with knobs, like the original using
[Arduino](http://www.arduino.cc/). 

<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=3ae1978ffd&photo_id=4200386988"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=3ae1978ffd&photo_id=4200386988" height="300" width="400"></embed></object>

To get Java communicate via Serial Port there are two options,
[JavaComm](http://java.sun.com/products/javacomm/) API and
[rxtx](http://users.frii.com/jarvi/rxtx/) API. Arduino uses rxtx so I
went with that. Navigate to you Arduino installation folder, for Mac OS X
navigate into the Arduino.app,

    open /Applications/Arduino.app/Contents/Resources/Java/

From the folder copy,

 - RXTXcomm.jar
 - librxtxSerial.jnilib

to somewhere on your classpath. Do not compile rxtx yourself, when I
compiled it manually and put it on my classpath, Arduino IDE could not
connect to the board, use the one supplied with your IDE, Mac OS X users
make sure you are using 32 bit Java 1.5.0 otherwise you will get class
not found exceptions.

Open Arduino IDE and make a note of your serial port name, on my machine
its,

    (def arduino-port "/dev/tty.usbserial-A6008nhh")

In order to open the serial port for I/O we need to get a port
identifier from the API,

     (defn port-identifier []
       (let [ports (CommPortIdentifier/getPortIdentifiers)]
         (loop [port (.nextElement ports)
                name (.getName port)]
           (if (= name arduino-port)
             port (recur (.nextElement ports) (.getName port))))))

Using the identifier we can open the port for I/O, make sure baud rates
match between Clojure and Arduino,

     (defn open-port []
       (doto (.open (port-identifier) "pong" 10000) 
         (.setSerialPortParams 
          9600 SerialPort/DATABITS_8 SerialPort/STOPBITS_1 SerialPort/PARITY_NONE)
         ))

Now everything is set to read from the port,

     (defn poll-port [p]
       (with-open [in (BufferedReader. (InputStreamReader. (.getInputStream p)))]
         (.readLine in)))

Arduino will map knob reading between 0 and board size and will send
coordinate pairs every 50 milliseconds, when poll-port is called, it
will return,

    "100:200"

meaning player one is at y 100 and player two is at y 200. We modify
move-player function to check serial for input, parse the coordinates
returned and set the player positions accordingly,

     (defn move-player [coords player]
       (try
        (let [p1-y (BigInteger. (first (re-split #":" coords)))
              p1-x (:x (:1 @player))
              p1-src (:src (:1 @player))
              p2-y (BigInteger. (second (re-split #":" coords)))
              p2-x (:x (:2 @player))
              p2-src (:src (:2 @player))]
          (dosync (alter player merge 
                         {:1 {:x p1-x :y p1-y :src p1-src } 
                          :2 {:x p2-x :y p2-y :src p2-src}})) )
        (catch Exception e)))

Because we don't listen for key inputs anymore, we need to modify
actionPerformed call and move players before every repaint,

        (actionPerformed 
          [e] 
          (move-player (poll-port serial) player)
          (move-ball ball)
          (wall-collision ball player)
          (player-collision ball player)
          (.repaint this))

Thats all the modification needed in our pong game. Code for the Arduino
is even simpler,

     const int boardheight = 400;
     const int p1Potpin = 0;
     const int p2Potpin = 1;

     void setup(){
       Serial.begin(9600);
     }

     void loop(){

       int p1 = analogRead(p1Potpin);
       int p2 = analogRead(p2Potpin);

       Serial.print(map(p1,0,1024,0,400));
       Serial.print(":");
       Serial.println(map(p2,0,1024,0,400));

       delay(50);
     }

We just read read the potentiometers every 50 milliseconds, map the
reading between 0 and board height and write it to serial. Hardware
setup looks like the following, fritzing project is also available check
below for a list of files.

![pong](http://farm3.static.flickr.com/2727/4200459126\_5f34e7e1b5\_o.png)

#### Files
 - [pong.clj](/code/arduino/pong/pong.clj)
 - [pong.pde](/code/arduino/pong/pong.pde)
 - [pong.fz](/code/arduino/pong/pong.fz)
