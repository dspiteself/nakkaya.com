---
title: Towards a Clojure Autopilot - First Steps
tags: clojure flightgear
---

[Quadrotors](http://en.wikipedia.org/wiki/Quadrotor) are all the rage
these days, thats why we decided to build one. The idea is to program
the autopilot in Clojure and run it on board the quadrotor using a
BeagleBoard. Since building one is going to cost couple hundred bucks
and nothing I write works the first time round, I would like a way to
test the controller without endangering the real thing. This is where
[FlightGear](http://www.flightgear.org/) an open source flight simulator
comes into play, FlightGear allows external applications to control the
aircraft which makes it a great simulation environment. This document
goes over the process of creating a simple [proportional
controller](http://en.wikipedia.org/wiki/Proportional_control) for
stabilizing the aircraft.

<p id='preview-auto'>You should see a video here...</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','640','480','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/simple-autopilot.m4v');
	s1.write('preview-auto');
</script>

Programs can interact with FlightGear through FlightGear's [property
tree](http://wiki.flightgear.org/index.php/Property_Tree), it contains
all the information about the aircraft and the game environment, using
FlightGear's [generic
protocols](http://wiki.flightgear.org/index.php/Generic_Protocol) we can
define a data exchange scheme that allows us to get/set values in the
property tree. We define two protocols an input protocol which allows us
to control the aileron and the elevator on the plane and an output
protocol that tells FlightGear to send us sensor readings for [roll and
pitch](http://en.wikipedia.org/wiki/Yaw,_pitch,_and_roll). All
communication is done over UDP.

     (defn controller [roll pitch]
       (let [roll-cntrl (float (map-number roll 90 -90 -1 1))
             pitch-cntrl (float (map-number pitch -45 45 -1 1))]
         (println "Control: " roll roll-cntrl pitch pitch-cntrl)
         [roll-cntrl pitch-cntrl]))

What the controller needs to do is keep the roll and pitch angle of the
aircraft at 0 degrees, roll angle is a number thats between 180, -180,
ailerons which control the roll angle takes a value between 1, -1
causing one to go down and one to go up. If the current roll angle is
not between the range 90, -90 we turn them all the way once we are in
range we map the roll angle to a number between -1 and 1 this way as we
reach 0 degree roll we make smaller and smaller adjustments. We do the same
for pitch.

     (defn control-loop [active]
       (let [socket-in (DatagramSocket. fg-port-out)
             buffer-in (byte-array 2048)
             packet-in (DatagramPacket. buffer-in (count buffer-in))
             socket-out (DatagramSocket.)]
         (in-thread
          #(try
             (while @active
               (.receive socket-in packet-in)
               (let [state (read-string
                            (String. buffer-in 0 (dec (.getLength packet-in))))
                     [roll-cntrl pitch-cntrl] (apply controller state)]
                 (.setLength packet-in (count buffer-in))
                 (let [msg (.getBytes (str roll-cntrl \, pitch-cntrl "\n"))
                       packet (DatagramPacket. msg (count msg)
                                               fg-host fg-port-in)]
                   (.send socket-out packet))))
             (finally (.close socket-in)
                      (.close socket-out))))))

In our control loop we wait for a packet from FlightGear, from the
packet we extract the current state of the aircraft, calculate control
values for ailerons and elevators and send it. 

In order run this example, you need to place the xml files to the
folder,

    /path/to/FlightGear/data/Protocol/

and run FlightGear using the command at the bottom of the clj file.

Files,

 - [autopilot.clj](/code/clojure/flightgear/simple/autopilot.clj)
 - [input-protocol.xml](/code/clojure/flightgear/simple/input-protocol.xml)
 - [output-protocol.xml](/code/clojure/flightgear/simple/output-protocol.xml)
