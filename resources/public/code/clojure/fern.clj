(ns fern
  (:import (javax.swing JFrame JLabel)
	   (java.awt.image BufferedImage)
	   (java.awt Dimension Color)
	   (java.lang Math)
	   (java.util Random)))

(defstruct <point> :x :y)

(defn transform-one [ target ] 
  (struct <point> 0 (* 0.16 (:y target))))
(defn transform-two [ target ] 
  (struct <point> 
	  (- (* 0.2  (:x target)) (* 0.26 (:y target))) 
	  (+ (* 0.23 (:x target)) (* 0.22 (:y target)))))
(defn transform-three [ target ] 
  (struct <point>  
	  (+ (* -0.15 (:x target)) (* 0.28 (:y target)))  
	  (+ (* 0.26  (:x target)) (* 0.24 (:y target)) 0.44)  ))
(defn transform-four [ target ] 
  (struct <point> 
	  (+ (* 0.85   (:x target)) (* 0.04 (:y target)))
	  (+ (* -0.004 (:x target)) (* 0.85 (:y target)) 1.6)  ))

(defn transform 
  "Transform point accourding to the percentage."
  [ target ] 
  (let  [ random (new Random) 
	  percentage (.nextInt random 100) ] 
    (cond 
     (<= percentage 1 ) (transform-one target)
     (<= percentage 7 ) (transform-two target)
     (<= percentage 14 ) (transform-three target)
     (<= percentage 100 ) (transform-four target) )))

(defn paint-point 
  "Paint point on canvas"
  [ width height target graphics ] 
  (let  [ scale (int (/ height 11))
	  y  (- (int (- height 25)) (int ( * scale (:y target)))) 
	  x  (+ (int (/ width 2))    (int ( * scale (:x target)))) ] 
    (.drawLine graphics x y x y ) ))

(defn draw-fern [ width height max-points graphics ] 
  (let [ coords  (take max-points (iterate transform (struct <point> 0 1)))]
    (doseq [coord coords]
      (paint-point width height coord graphics )) ))

(defn draw [width height points]
  (let [frame  (new JFrame)
	image  (new BufferedImage width height BufferedImage/TYPE_INT_RGB )
	canvas (proxy [JLabel] []
		 (paint [g]			  
			(.drawImage g image 0 0 this) ))
	graphics (.createGraphics image)]
    (.setColor graphics Color/green)
    (draw-fern width height points graphics)
    (.add frame canvas)
    (.setSize frame (new Dimension width height))
    (.show frame)))

(draw 400 400 100000)
