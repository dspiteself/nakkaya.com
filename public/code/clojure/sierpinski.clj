(ns sierpinski
    (:import (java.awt Dimension Polygon)
	     (javax.swing JFrame JLabel)
	     (java.awt Color)
	     (java.awt.image BufferedImage)))

(defstruct point :x :y)
(defstruct triangle :left :rigth :bottom)

(defn paint-triangle [g trig]
  (let  [polygon (new Polygon)]
    (doto polygon
          (.addPoint  (:x (:left trig))   (:y (:left trig)))
	  (.addPoint  (:x (:rigth trig))  (:y (:rigth trig)))
	  (.addPoint  (:x (:bottom trig)) (:y (:bottom trig))))
    (.drawPolygon g polygon)))

(defn midpoints [trig]
  (struct triangle 
	  (struct point
		  (int (/ (+ (:x (:bottom trig)) (:x (:left trig))) 2 ))
		  (int (/ (+ (:y (:bottom trig)) (:y (:left trig))) 2 )))
	  (struct point
		  (int (/ (+ (:x (:rigth trig)) (:x (:bottom trig))) 2 ))
		  (int (/ (+ (:y (:rigth trig)) (:y (:bottom trig))) 2 )))
	  (struct point
		  (int (/ (+ (:x (:left trig)) (:x (:rigth trig))) 2 ))
		  (int (/ (+ (:y (:left trig)) (:y (:rigth trig))) 2 )))))

(defn create-triangles [trig step depth g]
  (paint-triangle g trig)
  (let  [points (midpoints trig) 
	 left   (struct 
		 triangle (:left trig) (:left points) (:bottom points))
	 rigth  (struct 
		 triangle (:rigth trig) (:rigth points) (:bottom points))
	 top    (struct 
		 triangle (:bottom trig) (:rigth points) (:left points)) ]
    (if (< step depth )
      (do (create-triangles left  (inc step) depth g)
	  (create-triangles rigth (inc step) depth g)
	  (create-triangles top   (inc step) depth g)))))

(defn draw [depth width height]
  (let [frame  (new JFrame)
	image  (new BufferedImage width height BufferedImage/TYPE_INT_RGB )
	canvas (proxy [JLabel] [] (paint [g] (.drawImage g image 0 0 this)))
	graphics (.createGraphics image)]
    (.setColor graphics Color/white)

    (create-triangles 
     (struct 
      triangle
      (struct point 0 height)
      (struct point width height)
      (struct point (/ width 2) 0)) 
     0 depth graphics)

    (.add frame canvas)
    (.setSize frame (new Dimension width height))
    (.show frame)))

(draw 4 400 400)
