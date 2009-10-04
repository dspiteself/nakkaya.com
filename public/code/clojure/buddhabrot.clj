(ns buddhabrot
  (:import (javax.swing JFrame JLabel)
	   (java.awt Graphics)
	   (java.awt.image BufferedImage)
	   (java.awt Dimension Color)
	   (java.lang Math)
	   (java.awt Color)
	   (java.util Random)))

(defstruct complex :real :imag)

(defn add
  "Complex addition"
  [ c1 c2 ] 
  (struct complex 
	  (+ (:real c1) (:real c2))
	  (+ (:imag c1) (:imag c2))))

(defn multiply
  "Complex Multipication"
  [ c1 c2 ] 
  (struct complex 
	  (- (* (:real c1) (:real c2)) (* (:imag c1) (:imag c2)))
	  (+ (* (:real c1) (:imag c2)) (* (:imag c1) (:real c2)))))

(defn abs
  "Complex Absulute Value"
  [ complex ]
  (Math/sqrt 
   (+ (* (:real complex) (:real complex) ) 
      (* (:imag complex) (:imag complex)))))


(defn calc-path
  [ x y max-iterations ] 
  (let  [ c (struct complex x y) ]
    (loop [ z c 
	    path [  ]
	    iterations 0 ]
      (if  (> iterations max-iterations)
	[]
	(if (> (abs z ) 2.0 ) 
	  (conj path z)
	  (recur (add c (multiply z z)) (conj path z) (inc iterations)))))))

(defn point-to-coordinate [fractal point]
  [(int (+ (* 0.3 
	      (:width fractal) 
	      (+ (:real point) 0.5)) 
	   (/ (:width fractal) 2)))
	    
   (int (+ (* 0.3 
	      (:height fractal) 
	      (:imag point) ) 
	   (/ (:height fractal) 2)))])

(defn buffer-set [fractal point]
  (let  [buffer (:buffer fractal)
	 coord (point-to-coordinate fractal point)
	 x (first coord)
	 y (second coord)]    
    (if  (and (> x 0) (> y 0)
	      (< x (:width fractal)) (< y (:height fractal)))
	(aset buffer  y x (+ 1 (aget buffer y x ))) ) ))

(defn generate [fractal]
  (let  [buffer (:buffer fractal)
	 rand   (new Random)]
    (doseq [point (iterate inc 1)]
      (let  [x    (- (* 6 (.nextDouble rand)) 3) 
	     y    (- (* 6 (.nextDouble rand)) 3)
	     path   (calc-path x y (:iteration fractal))]

	(if (= (mod point 1000000) 0)
	  (println "Point: " point))

	(doseq [p path] (buffer-set fractal p)))) ))

(defn start [fractal]
  (.start (Thread. (proxy [Runnable] [] (run [] (generate fractal))))))

(defn calc-pixel-color
  [ iteration max-iterations ] 
  (let  [ gray (int (/ (* iteration 255) max-iterations )) 
	  r    gray
	  g    (Math/min (int ( / (* 5 ( * gray gray)) 255)) 255)
	  b    (Math/min (int (+ 40 ( / (* 5 (* gray gray)) 255))) 255) ]
    (try (new Color r g b ) (catch Exception e (new Color 0 0 0))) ))

(defn paint-canvas [fractal graphics] 
  (let  [buffer (:buffer fractal)
	 biggest  (apply max (map #(apply max %) buffer)) ]
    (doseq [y (range (:height fractal))
	    x (range (:width fractal)) ]
     
      (if  (> (aget buffer y x) 0 )
      	(do
      	  (.setColor graphics (calc-pixel-color (aget buffer y x) biggest ))
      	  (.drawLine graphics x y x y ))))))

(defn draw [fractal]
  (let [frame  (new JFrame)
	image  (new BufferedImage 
		    (:width fractal) (:height fractal) 
		    BufferedImage/TYPE_INT_RGB)
	canvas (proxy [JLabel] [] (paint [g] (.drawImage g image 0 0 this)))
	graphics (.createGraphics image)]

    (paint-canvas fractal graphics)

    (.add frame canvas)
    (.setSize frame (new Dimension (:width fractal) (:height fractal)))
    (.show frame)))

(def fractal {:buffer (make-array Integer/TYPE 800 800)
	      :width 800 :height 800 :iteration 50})
(start fractal)
;(draw fractal)
