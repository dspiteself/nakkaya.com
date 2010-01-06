---
title: Making Things Move with Clojure
tags: clodiuno clojure arduino
---

I have added analog write support to
[clodiuno](http://github.com/nakkaya/clodiuno), which can be used to
send a [PWM](http://en.wikipedia.org/wiki/Pulse-width_modulation) value or
control a [servo](http://en.wikipedia.org/wiki/Servomechanism). Servos
are the easiest way to play with motor control. Even though they don't
turn 360 degrees, they can be used to create mechanisms such as levers
and cams. Firmata library supports servos on pins 2 through 13,
following code demonstrates how to control a servo using values read
from an analog input in this case a
[potentiometer](http://en.wikipedia.org/wiki/Potentiometer).


Connections for the board looks like the following, fritzing project is
also available [here](/code/clodiuno/servo/servo.fz),

![servo setup](http://farm3.static.flickr.com/2682/4249344881\_f068095571\_o.png)


Code is made up of two functions, one of which I stole directly from the
Arduino libraries,

     ;;WMath.cpp
     (defn map-range [x in-min in-max out-min out-max]
       (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))

This is a Clojure version of the Arduino's map function, it will map
the number in the in range into out range. Analog read returns a number
between 0 and 1023 but the servo expects values between 0 and 179, this
will turn the potentiometer reading into an angle for the servo.

     (defn servo []
       (let [board (arduino "/dev/tty.usbserial-A6008nhh")]
         ;;allow board to boot
         (Thread/sleep 5000)
         ;;start reading potentiometer
         (enable-pin board :analog pot-pin)
         ;;attach servo
         (pin-mode board servo-pin SERVO)
         ;;
         (while 
          true
          (let [pot (analog-read board pot-pin)
                angle (int (map-range pot 0 1023 0 179))]
            (analog-write board servo-pin angle)))
         ;;
         (close board)))


First we need to tell Firmata to start reporting readings for the
potentiometer,

    (enable-pin board :analog pot-pin)

Then we set servo pin to SERVO this will make Firmata to attach a servo on
that pin,

    (pin-mode board servo-pin SERVO)

Now we are ready to read input, map it into an angle, then write it to servo,

         (while 
          true
          (let [pot (analog-read board pot-pin)
                angle (int (map-range pot 0 1023 0 179))]
            (analog-write board servo-pin angle)))

#### Files

 - [servo.clj](/code/clodiuno/servo/servo.clj)
 - [servo.fz](/code/clodiuno/servo/servo.fz)

<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=756ac83e82&photo_id=4249343911"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=756ac83e82&photo_id=4249343911" height="300" width="400"></embed></object>
