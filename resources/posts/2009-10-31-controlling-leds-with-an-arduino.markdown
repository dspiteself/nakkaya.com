---
title: Controlling LEDs with an Arduino
tags: arduino
---

This post will cover the process of controlling multiple LED's using an
Arduino. With this hack we'll make the Arduino, send a distress signal
using a LED, a second LED is used to indicate the end of SOS cycle.


#### Hook-up

I used the following hook-up,

![circuit](/images/post/arduino-led-circuit.jpeg)

Both LED's are connected the same way except to different pins (Digital
10 and 9). +5V is connected to the resistor which is connected to the +
(long leg) of the LED, - (short leg) of the LED is connected to the
ground.

Any resistor that is bigger than 330 ohms can be used, but keep in mind
that the smaller the resistance the bigger the glow.

![circuit](/images/post/arduino-led-circuit-2.jpeg)

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

To represent the letters, I used an array, 1 to represent a long
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

<p id='preview'>Player</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','400','300','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/arduino-led.mp4');
	s1.write('preview');
</script>
