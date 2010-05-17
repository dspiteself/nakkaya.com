---
title: Poor Man's Arduino Line Follower
tags: arduino
---

> For a faster smarter line follower checkout [Arduino Line Follower Take Two](/2010/05/18/arduino-line-follower-take-two/)

Over the weekend, me and a friend of mine, cannibalized a 9 Lira (6$) RC
car into line a follower. This post covers the process of building it.

Code for the robot is at github, code covers, commit
[cf731aedc156f067aa221...](http://github.com/nakkaya/corba/tree/cf731aedc156f067aa221fc5486e4e1f8761785d) Refer
to this particular commit for this project cause we plan on building a
faster one hence code will change.

For this project we used,

 - 3x [QTR-1RC Reflectance Sensor](http://www.pololu.com/catalog/product/959)
 - [Ardumoto - Motor Driver Shield](http://www.sparkfun.com/commerce/product_info.php?products_id=9213)
 - A cheap RC car

![hardware](http://farm3.static.flickr.com/2628/4170781364_68408bcc0c.jpg)

#### Hardware

Wiring looks like the following,

![line follower](http://farm5.static.flickr.com/4038/4169796553_1e568d9905.jpg)

Fritzing project can be downloaded
[here](/code/arduino/line-follower/line-follower-1.fzz). Sorry about the
crappy diagram, but I just suck at it.


Most important part of the robot are the reflection sensors. two sensors
track black area to the sides one sensor tracks white line in the
middle. Each sensor has 3 pins on them, VIN GROUND and OUT. VIN and
GROUND are connected to VIN and GROUND on the arduino. OUT goes to
digital pins on the arduino. For this project we used, pins 5 to 7.

 - Sensor on the left is connected to digital pin 5
 - Sensor in the middle is connected to digital pin 6
 - Sensor on the right is connected to digital pin 7

![sensors](http://farm3.static.flickr.com/2647/4170781644_49f959c24d.jpg)

An LED is connected to digital pin 8, and a push button is connected to
digital pin 2, refer to the fritzing diagram for their wiring.

To control the DC motors on the RC car, we used a ardumoto shield which
allows you to control up to 2 DC motors. It uses digital pins,

 - 10 for PWM for MotorA
 - 11 for PWM for MotorB
 - 12 for Direction MotorA
 - 13 for Direction MotorB

The way our car setup is, motor on the rear provides forward and
backward motion depending on the direction of the motor, motor on the
forward however turns the wheels depending on the direction it is
turning (e.g. when the direction pin is high wheels turn right).

Whole thing is powered using a 9V 2Amp power adapter.

#### Software

Control software is made up of 2 modules, engine module and a navigation
module. Engine module is responsible for movement it exposes five
functions, straight, left, right, reverse and forward. Navigation module
exposes 2 functions calibrate and steer, steer function determines the
robots position and command engine to move in the correct direction.

     void engine::forward(int speed, int time){
       analogWrite(PwmPinMotorB, speed);
       digitalWrite(DirectionPinMotorB, LOW);

       delay(time);

       analogWrite(PwmPinMotorB, 0);
       digitalWrite(DirectionPinMotorB, LOW);
     }

     void engine::reverse(int speed, int time){
       analogWrite(PwmPinMotorB, speed);
       digitalWrite(DirectionPinMotorB, HIGH);

       delay(time);

       analogWrite(PwmPinMotorB, 0);
       digitalWrite(DirectionPinMotorB, HIGH);
     }

To move the robot forward and backward, these functions are used, they
take a PWM value and a time in milliseconds,

    engine.forward(255,10);

will turn the motor in the back for 10 milliseconds at full
power. Direction of the robot can be changed using,

     void engine::right(){
       analogWrite(PwmPinMotorA, 255);
       digitalWrite(DirectionPinMotorA, LOW);
     }

     void engine::left(){
       analogWrite(PwmPinMotorA, 255);
       digitalWrite(DirectionPinMotorA, HIGH);
     }

     void engine::straight(){
       analogWrite(PwmPinMotorA, 0);
       digitalWrite(DirectionPinMotorA, HIGH);
     }

These will change the direction of the forward motor depending on the
direction or cut power if we want to go straight. The engine is
controlled from within the navigation module.


Navigation module begins, by calibrating itself,

     void navigation::calibrate(){
       unsigned int val[3];
       qtr.read(val);
  
       int right = val[0];
       int middle = val[1];
       int left = val[2];

       bwMean = ((right - middle) + (left - middle))/2;
     }

Pololu's library includes a function to calibrate the sensors but we
decided not to use it for the moment, since we can't precisely control
robots direction, it always goes off course during calibration. What we
do instead is read all three sensors and calculate the average of
difference in black and white readings, which is stored in a variable
called bwMean which is used when calculating robots position relative to
the line.

     void navigation::steer(){
       int bearing = getBearing();

       if(bearing == LEFT)
         engin.left();

       if(bearing == STRAIGHT)
         engin.straight();

       if(bearing == RIGHT)
         engin.right();

       engin.forward(255,10);
     }

steer function is where the movement happens, it asks for a position,
depending on the position, turn the front wheel and move one step
forward.


     int navigation::getBearing(){
       unsigned int val[3];
       qtr.read(val);

       int right = val[0];
       int middle = val[1];
       int left = val[2];

       if(left <= middle && left <= right && position != RIGHT){
         position = LEFT;
         return RIGHT;
       }

       if(right <= left && right <= middle && position != LEFT){
         position = RIGHT;
         return LEFT;
       }

       if(middle <= left && middle <= right && middle < bwMean){
         position = STRAIGHT;
         return STRAIGHT;
       }

       if (position == LEFT ) return RIGHT;
       if (position == RIGHT ) return LEFT;
     }

getBearing is where we calculate our position relative to the line. We
read all three sensors, basically the sensor with the lowest value means
it is on the white line. So we turn in the direction of the sensor with
the lowest value, but there is a problem with this approach, when all
sensors are on the black area, one of them will still read lower than the
others and we will turn in some random direction. To overcome this
problem we introduce a new variable called position which records last
position. Now we only turn right or left if we are not already in that
direction of the line, e.g. we don't want to turn left when we are
already left of the line. This covers left and right turns however when
all the sensors are on the black, middle one will still read lowest so
we use the bwMean value we calculated and only go straight if the middle
sensor reading is lower than the mean value we calculated. If we are
left or right of the line we continue turning in that direction, until
we find the line again.

In the main arduino loop we just call steer function over and over again,

     void loop(){
       navigation.steer();
       delay(70);
     }

When everything wired and code uploaded, it works like this,

<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=86bc46b5b9&photo_id=4163125335"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=86bc46b5b9&photo_id=4163125335" height="300" width="400"></embed></object>

For more pictures and videos check out my flickr
[set](http://www.flickr.com/photos/nakkaya/sets/72157622790593009/).
