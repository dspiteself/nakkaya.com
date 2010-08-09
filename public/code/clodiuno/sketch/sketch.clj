(ns sketch
  (:use turtle)
  (:use clodiuno.core)
  (:use clodiuno.firmata)
  (:import (javax.swing JFrame JPanel)
	   (java.awt Color Rectangle)
	   (java.awt.event ActionListener KeyAdapter)
	   (javax.swing Timer)
	   (java.awt.geom RoundRectangle2D$Double Ellipse2D$Double)))

(def potx-pin 0)
(def poty-pin 1)

(def window-width 640)
(def window-height 480)
(def pad 20)
(def sketch-width 600)
(def sketch-height 400)

;;WMath.cpp
(defn map-range [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))

(defn read-knobs [board]
  (let [potx (analog-read board potx-pin)
	poty (analog-read board poty-pin)
	x (int (map-range 
		potx 0 1024 (- (/ sketch-width 2)) (/ sketch-width 2)))
	y (int (map-range 
		poty 0 1024 (- (/ sketch-height 2)) (/ sketch-height 2)))]
    {:x x :y y}))

(defn panel [board turtle]
  (proxy [JPanel ActionListener] []
    (paintComponent
     [g]
     (doto g
       (.setColor Color/red)
       (.fill (RoundRectangle2D$Double.
	       0 0 window-width window-height 60 60))
       (.setColor Color/black)
       (.drawImage (:image @turtle) pad pad this)
       (.setColor Color/white)
       (.fill (Ellipse2D$Double. pad 430 40 40))
       (.fill (Ellipse2D$Double. (- sketch-width pad) 430 40 40))))
    (actionPerformed 
     [e] 
     (let [knobs (read-knobs board)]
       (go turtle (:x knobs) (:y knobs)))
     (.repaint this))))

(defn key-listener [board frame timer]
  (proxy [KeyAdapter] [] 
    (keyReleased 
     [e]
     (.stop timer)
     (close board)
     (.setVisible frame false))))

(defn init-arduino []
  (let [board (arduino :firmata "/dev/tty.usbserial-A6008nhh")]
    (Thread/sleep 5000)
    (enable-pin board :analog potx-pin)
    (enable-pin board :analog poty-pin)
    board))

(defn init-turtle [board turtle]
  (let [knobs (read-knobs board)]
    (doto turtle
      (pen-up)
      (go (:x knobs) (:y knobs))
      (pen-down))))

(defn sketch []
  (let [board  (init-arduino)
	turtle (turtle sketch-width sketch-height)
	panel  (panel board turtle)
	timer  (Timer. 50 panel)
	window (JFrame.)]
    (doto window
      (.add panel)
      (.addKeyListener (key-listener board window timer))
      (.setBackground (Color. 0 0 0 0))
      (.setUndecorated true)
      (.setAlwaysOnTop true)
      (.setSize (java.awt.Dimension. window-width (+ 25 window-height)))
      (.setVisible true))
    (init-turtle board turtle)
    (.start timer)))

(sketch)
