---
title: Controlling LEDs with an Arduino
tags: arduino
---

This post will cover the process of controlling multiple LED's using an
Arduino. With this hack we'll make the Arduino, send a distress signal
using a LED, a second LED is used to indicate the end of SOS cycle.


#### Hook-up

I used the following hook-up,

![circuit](http://farm3.static.flickr.com/2478/4059787347_3566ecebb3.jpg)

Both LED's are connected the same way except to different pins (Digital
10 and 9). +5V is connected to the resistor which is connected to the +
(long leg) of the LED, - (short leg) of the LED is connected to the
ground.

Any resistor that is bigger than 330 ohms can be used, but keep in mind
that the smaller the resistance the bigger the glow.


![circuit](http://farm4.static.flickr.com/3256/4060493030_5c6e2fd9c0.jpg)

Completed circuit looks like the picture above.

#### Code


First some definitions,

    int redLEDPin = 10;
    int yellowLEDPin = 9;

I choose to use pin 10 for the red LED, pin 9 for yellow LED.

    int shortPulse = 250;
    int longPulse = 125;
    int letterDelay = 1000;

Morse code uses a short element and a long element to represent letters
and numbers. We will use 250 ms for long element 125 ms for short
element, after each letter we'll have 1 second delay.

    //letters 0 for short pulse 1 for long
    int letterS[] = {0, 0, 0};
    int letterO[] = {1, 1, 1};

To represent the letters, i used an array, 1 to represent a long
element, 0 to represent a short element.

    void blinkLetter(int* letter){
      for(int i=0; i<3; i++){
        int val = letter[i];

        if(val == 0)
          redLed(shortPulse);
        else
          redLed(longPulse);
      }
      delay(letterDelay);
    }

blinkLetter takes an array as it's argument, it will iterate over the
array blink the red LED accordingly.

    void redLed(int time){
        digitalWrite(redLEDPin, HIGH);
        delay(time);
        digitalWrite(redLEDPin, LOW);
        delay(time);
    }

redLed function takes a time variable as its input it will keep the LED
on for the given time.

    void yellowLed(){
        digitalWrite(yellowLEDPin, HIGH);
        delay(2000);
        digitalWrite(yellowLEDPin, LOW);
    }

yellowLed uses a fixed time to signal us that the SOS cycle is complete,
we are starting over.

    void loop() { 
      blinkLetter(letterS);
      blinkLetter(letterO);
      blinkLetter(letterS);

      yellowLed();
    }

Main loop just cycles through the letters and blink the yellow LED to
signal us that the cycle is complete.

 - Code can be downloaded [here](/code/arduino/sos/sos.pde)
 - Schematics can be downloaded [here](/code/arduino/sos/sos.fz)

When the circuit is hooked-up and code uploaded, it should look like
this.

<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=2cedf2491c&photo_id=4060504016"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=2cedf2491c&photo_id=4060504016" height="300" width="400"></embed></object>
