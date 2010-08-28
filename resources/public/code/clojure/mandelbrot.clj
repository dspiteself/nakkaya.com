(ns mandelbrot
  (:refer-clojure :exclude [+ * <])
  (:use (clojure.contrib complex-numbers)
        (clojure.contrib.generic [arithmetic :only [+ *]]
                                 [comparison :only [<]]
                                 [math-functions :only [abs]]))
  (:import (javax.swing JFrame JLabel)
	   (java.awt.image BufferedImage)
	   (java.awt Dimension Color)))

(defn calc-iterations [p q max-iterations]
  (let  [ c (complex p q) ]
    (loop [ z c 
	    iterations 0 ]      
      (if  (or (> (abs z ) 2.0 ) (> iterations max-iterations) )
	(if  ( = 0 iterations ) 0 (- iterations 1 ))
	(recur (+ c (* z z) ) (inc iterations)) ))))

(defn calc-pixel-color [iterations max-iterations]
  (if  (< iterations 10  )
    (new Color 0 0 0 )
    (if  (= iterations max-iterations  )
      (new Color 0 0 0 )
  (let  [ gray (int (/ (* iterations 255) max-iterations )) 
	  r    gray
	  g    (Math/min (int ( / (* 5 ( * gray gray)) 255)) 255)
	  b    (Math/min (int (+ 40 ( / (* 5 (* gray gray)) 255))) 255) ]
    (new Color r g b )))))

(defn generate [x y width height max-iterations 
		graphics surface-width surface-height]
  (doseq [i (range surface-width )
	  j (range surface-height )]
      (let  [p  ( + x (* width (/ i (float surface-width))))
	     q  ( + y (* height (/ j (float surface-height)))) 
	     iterations (calc-iterations p q max-iterations) 
	     color (calc-pixel-color iterations max-iterations) ]

	(.setColor graphics color)
	(.drawLine graphics i j i j)  )))

(defn draw [ x y width height iterations surface-width surface-height]
  (let [frame  (new JFrame)
	image  (new BufferedImage 
		    surface-width surface-height 
		    BufferedImage/TYPE_INT_RGB )
	canvas (proxy [JLabel] []
		 (paint [g]  
			(.drawImage g image 0 0 this) ))
	graphics (.createGraphics image)]
    (.setColor graphics Color/green)
    (generate x y width height iterations 
	      graphics surface-width surface-height)
    (.add frame canvas)
    (.setSize frame (new Dimension surface-width surface-height))
    (.show frame)))

(draw -2.1 -1.4 3.0 3.1 100 400 400)
