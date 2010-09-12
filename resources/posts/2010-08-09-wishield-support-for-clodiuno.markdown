---
title: WiShield Support for Clodiuno
tags: clojure clodiuno
---

I have added support for multiple protocols along with an implementation
for
[WiShield](http://asynclabs.com/store?page=shop.product_details&flypage=flypage.tpl&product_id=26&category_id=6),
which provides Wi-Fi connectivity to Arduino and allows you to get rid
of that USB cable while using Clodiuno.

Following is a quick snippet that demonstrates both protocols in action,
we read the potentiometer value over Wi-Fi from an Arduino and write it
through Firmata to another Arduino,

     (ns wishield-to-firmata
       (:use clodiuno.core)
       (:use clodiuno.firmata)
       (:use clodiuno.wishield))

     (defmacro forever [& body] `(try (while true  ~@body) (catch Exception e#)))

     (defn map-int [x in-min in-max out-min out-max]
       (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))

     (def wishield (arduino :wishield "10.0.2.100" 1000))
     (def firmata (arduino :firmata "/dev/tty.usbserial-A6008nhh"))

     ;;allow firmata to boot
     (Thread/sleep 5000)

     (pin-mode wishield 5 ANALOG)
     (pin-mode firmata 3 PWM)

     (forever
      (let [pot-val (analog-read wishield 5)
            pwm-val (int (map-int pot-val 0 1023 0 255))] 
        (println pot-val)
        (analog-write firmata 3 pwm-val)))

     ;;(close firmata)
     ;;(close wishield)

<p id='preview'>Player</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','400','300','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/wishield-demo.mp4');
	s1.write('preview');
</script>
