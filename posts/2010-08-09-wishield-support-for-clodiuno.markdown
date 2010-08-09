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


<object type="application/x-shockwave-flash" width="400" height="300" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=021d617a3b&photo_id=4873878770"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=021d617a3b&photo_id=4873878770" height="300" width="400"></embed></object>
