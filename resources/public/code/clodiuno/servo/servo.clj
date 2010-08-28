(ns servo
  (:use :reload-all clodiuno.core)
  (:use :reload-all clodiuno.firmata))

;;WMath.cpp
(defn map-range [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))

;;analog 0
(def pot-pin 0)
;;digital 2
(def servo-pin 2)

(defn servo []
  (let [board (arduino :firmata "/dev/tty.usbserial-A6008nhh")]
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

(servo)
