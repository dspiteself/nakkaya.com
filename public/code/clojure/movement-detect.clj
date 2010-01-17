(ns movement-detect
  (:import (javax.swing JFrame JLabel Timer)
	   (java.awt.event ActionListener KeyAdapter)
	   (java.awt Canvas Image Color BasicStroke)
	   (java.awt.image MemoryImageSource)
	   (hypermedia.video OpenCV)))

(def frame-rate (int 1000/30))
(def width 640)
(def height 480)
(def circle-radius 100)

(defn create-circle []
  (let [x (rand-int (- width circle-radius))
	y (rand-int (- height circle-radius))]
    {:x x :y y :area (for [y (range y (+ y circle-radius))
			   x (range x (+ x circle-radius))] [x y])}))

(defn vision []
  (doto (OpenCV.)
    (.capture width height)))

(defn capture-image [vis]
  (.read vis)
  (let [raw (.pixels vis)] 
    (doto vis
      (.absDiff)
      (.convert OpenCV/GRAY)
      (.blur OpenCV/BLUR 3)
      (.threshold 20)
      (.remember))
    {:raw raw :diff (.pixels vis)}))

(defn white-pixel-count [circle pixels]
  (reduce (fn[h v]
	    (let [x (first v) y (second v)
		  pix (nth pixels (+ x (* y width)))
		  blue (bit-and pix 0x000000ff)]
	      (if (= blue 255) (inc h) h))) 0 (:area circle)))

(defn collision? [circle pixels]
  (let [white (white-pixel-count circle pixels)] 
    (cond (zero? white) false
	  (> (/ 1 (/ (count (:area circle)) white)) 0.2) true
	  :else false)))

(defn validate-circles [circles pixels]
  (reduce (fn[h c]
	    (if (collision? c pixels)
	      (conj h (create-circle))
	      (conj h c))) [] @circles))

(defn capture-action [vis panel image circles]
  (proxy [ActionListener] []
    (actionPerformed
     [e]
     (let [capture (capture-image vis)]
       (dosync (ref-set image capture)
	       (ref-set circles (validate-circles circles (:diff capture)))))
     (.repaint panel))))

(defn create-image [panel pixels]
  (let [mis (MemoryImageSource. width height pixels 0 width)]
    (.createImage panel mis)))

(defn panel [image circles]
  (let [canvas (Canvas.)] 
    (proxy [JLabel] [] 
      (paint
       [g]
       (let [raw (create-image canvas (:raw @image))
	     diff (create-image canvas (:diff @image))]
	 (.drawImage g raw 0 0 nil)
	 (.drawImage g diff width 0 nil))

       (.setColor g Color/red)
       (.setStroke g (BasicStroke. 10))
       (doseq [c @circles]
	 (.draw g (java.awt.geom.Ellipse2D$Double. 
		   (:x c) (:y c) circle-radius circle-radius)))))))

(defn key-listener [vis timer]
  (proxy [KeyAdapter] [] 
    (keyReleased 
     [e]
     (.stop timer)
     (.dispose vis))))

(defn main []
  (let [vis   (vision)
	image (ref (capture-image vis))
	circles (ref (take 5 (repeatedly create-circle)))
	panel (panel image circles)
	timer (Timer. frame-rate (capture-action vis panel image circles))]
    (.start timer)
    (doto (JFrame.)
      (.add panel)
      (.addKeyListener (key-listener vis timer))
      (.setSize (* 2 width) height)
      (.show))))

(main)
