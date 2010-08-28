(ns bang-bang.core
  (:import (org.ode4j.ode OdeHelper)
	   (javax.swing JFrame JPanel Timer)
	   (java.awt.event ActionListener)
	   (java.awt.geom Ellipse2D$Double)))
;;(OdeHelper/initODE2 0)

(defn world [g]
  (let [[x y z] g] 
    (doto (OdeHelper/createWorld)
      (.setGravity x y z))))

(defn body [world [x y z]]
  (let [body (OdeHelper/createBody world)
	mass (doto (OdeHelper/createMass)
	       (.setSphere 2500 0.05))] 
    (doto body
      (.setMass mass)
      (.setPosition x y z))))

(defn environment [world]
  (let [b1 (body world [1 2 0])
	b2 (body world [2 2 0])
	j1 (doto (OdeHelper/createHingeJoint world)
	     (.attach b1 nil)
	     (.setAnchor 0 2 0)
	     (.setAxis 0 0 1)
	     (.setParamVel 0)
	     (.setParamFMax 30))
	j2 (doto (OdeHelper/createBallJoint world)
	     (.attach b1 b2)
	     (.setAnchor 1 2 0))] 
    [b1 b2 j2 j1]))

(defn bang-bang [joint]
  (if (> (.getAngle joint) 2.5)
    (.setParamVel joint -0.1)
    (.setParamVel joint 2)))

(defn panel [world environment]
  (let [pos #(let [pos (.getPosition %)] [(.get0 pos) (.get1 pos)])
	coords #(vector (+ 100 (* 50 (first %))) (* 50 (second %)))
	circle #(let [rad 20 
		      offset (int (/ rad 2))
		      x (- %2 offset) 
		      y (- %3 offset)]
		  (.fill %1 (Ellipse2D$Double. x y rad rad)))]
    (proxy [JPanel ActionListener] [] 
      (paintComponent
       [g]
       (let [[b1 b2 _ j2] environment
	     [x1 y1] (coords (pos b1))
	     [x2 y2] (coords (pos b2))
	     [lx1 ly1] (coords [0 2])]
	 (.setColor g java.awt.Color/WHITE)
	 (.fillRect g 0 0 (.getWidth this) (.getHeight this))
	 (.setColor g java.awt.Color/BLACK)
	 (.drawString g (apply str "Angle: " 
			       (take 5 (str (.getAngle j2)))) 20 20)
	 (.drawLine g lx1 ly1 x1 y1)
	 (circle g x1 y1)
	 (.drawLine g x1 y1 x2 y2)
	 (circle g x2 y2)))
      (actionPerformed [e] 
		       (.repaint this)
		       (bang-bang (last environment))
		       (.step world 0.05)))))

(defn frame []
  (let [world (world [0 9.81 0])
	environment (environment world)
	panel (panel world environment)
	timer  (Timer. 50 panel)] 
    (doto (JFrame.)
      (.add panel)
      (.setVisible true)
      (.setSize 200 250)
      (.setAlwaysOnTop true))
    (.start timer)))


(comment
  (frame)
  )
