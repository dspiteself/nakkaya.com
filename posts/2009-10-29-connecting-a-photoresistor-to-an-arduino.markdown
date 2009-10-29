---
title: Connecting a Photoresistor to an Arduino
tags: arduino
---

A photoresistor is a resistor whose resistance decreases with increasing
incident light intensity, using a photoresistor we can make arduino
sense the intensity of light around it. Let's look at the hook up,

![diagram](/images/post/photoresistor.png)

Using the
[playground](http://www.arduino.cc/playground/Learning/PhotoResistor)
article as reference, resistor and photoresistor are connected in
series. +5V goes to resistor, ground goes to photoresistor, the junction
where the resistor and photoresistor meets goes to analog 0. Digital 13
is used for the LED.

![hookup](http://farm3.static.flickr.com/2438/4050068719_34efa0eefe.jpg)


![hookup](http://farm3.static.flickr.com/2546/4050068885_e3967f0742.jpg)

In my room photoresistor reads around 80, when i put my thumb on it
making it dark, it reads around 500. I wanted the LED to light up when
its dark.

    int lightPin = 0;  //define a pin for Photo resistor
    int threshold = 250;

    void setup(){
        Serial.begin(9600);  //Begin serial communication
        pinMode(13, OUTPUT);
    }

    void loop(){
        Serial.println(analogRead(lightPin)); 
    
        if(analogRead(lightPin) > threshold ){    
            digitalWrite(13, HIGH);
            Serial.println("high"); 
        }else{
            digitalWrite(13, LOW);
            Serial.println("low"); 
        }
    
        delay(100);
    }

So i picked a number in between and used it as threshold, if the reading
is above the threshold it turns the LED on when its below threshold it
turns the LED off.

And the result is,

<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=c8e9e39a1b&photo_id=4050815472"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=c8e9e39a1b&photo_id=4050815472" height="300" width="400"></embed></object>

Download [sketch](/code/arduino/photoresistor/photoresistor.pde) -
[fritzing](/code/arduino/photoresistor/photoresistor.fz)
