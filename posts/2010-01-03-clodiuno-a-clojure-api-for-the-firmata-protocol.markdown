---
title: clodiuno - A Clojure API for the Firmata Protocol
tags: clodiuno clojure arduino
---

[Firmata](http://www.firmata.org/wiki/Main_Page) is a protocol and a
firmware for [Arduino](http://www.arduino.cc/), it allows you to
control Arduino via a serial protocol from any language that has [serial
port](http://en.wikipedia.org/wiki/Serial_port) support. I had a lot of
free time during the holidays so I started to implement the protocol,
unfortunately protocol isn't well documented, so it took a while to get
a hang of it. For now not every thing is supported, I have implemented
digital read/writes and analog read.

You can grab a copy of clodiuno
[here](http://github.com/nakkaya/clodiuno). Of course no Arduino
introduction is complete with out blinking something, below snippet
should give you a feel for the API, there are more examples in the
examples folder included with the project.

     (ns sos
       (:use :reload-all clodiuno))

     (def short-pulse 250)
     (def long-pulse 500)
     (def letter-delay 1000)

     (def letter-s [0 0 0])
     (def letter-o [1 1 1])

     (defn blink [board time]
       (digital-write board 13 HIGH)
       (Thread/sleep time)
       (digital-write board 13 LOW)
       (Thread/sleep time))

     (defn blink-letter [board letter]
       (doseq [i letter]
         (if (= i 0)
           (blink board short-pulse)
           (blink board long-pulse)))
       (Thread/sleep letter-delay))

     (defn sos []
       (let [board (arduino "/dev/tty.usbserial-A6008nhh")] 
         ;;allow arduino to boot
         (Thread/sleep 5000)
         (pin-mode board 13 OUTPUT)

         (doseq [_ (range 3)] 
           (blink-letter board letter-s)
           (blink-letter board letter-o)
           (blink-letter board letter-s))
    
         (close board)))

This will make your Arduino call for help. Result will be similar to the
following but with a single LED.


<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=2cedf2491c&photo_id=4060504016&flickr_show_info_box=true"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=2cedf2491c&photo_id=4060504016&flickr_show_info_box=true" height="300" width="400"></embed></object>
