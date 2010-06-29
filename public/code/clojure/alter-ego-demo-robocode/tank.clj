(ns tank
  (:use [alter-ego.core])
  (:import (java.awt Color)
	   (robocode.util Utils))
  (:gen-class :extends robocode.AdvancedRobot :init init :state blackboard))

(defmacro forever [& body] 
  `(try (while true  ~@body) (catch Exception e#)))

(defn debug [s]
  (.println System/out s))

(def *safety* 200)
(def *gun-range* 250)

(defn execute [robot]
  (.execute robot) true)

(defn scan [blackboard]
  (from-blackboard blackboard [robot] 
		   (.setTurnRadarLeft robot 360)
		   (execute robot)))

(defn select-target [blackboard]
  (from-blackboard blackboard [enemies]
		   (dosync 
		    (alter blackboard assoc :target 
			   (first (sort-by :distance (vals enemies)))))))

(defn face-target [blackboard]
  (from-blackboard blackboard [robot target]
		   (if-not (nil? target)
		     (do (.setTurnRight robot (:bearing target))
			 (execute robot)))))

(defn ahead [blackboard]
  (from-blackboard blackboard [robot]
		   (.setAhead robot 100)
		   (execute robot)))

(defn back [blackboard]
  (from-blackboard blackboard [robot]
		   (.setBack robot 100)
		   (execute robot)))

(defn no-collision? [blackboard]
  (from-blackboard blackboard [robot target]
		   (if (nil? target)
		     true
		     (> (:distance target) *safety*))))

(defn target-in-range? [blackboard]
  (from-blackboard blackboard [robot target]
		   (if (nil? target)
		     false
		     (< (:distance target) *gun-range*))))

;;http://robowiki.net/wiki/Linear_Targeting
(defn lock-gun [blackboard]
  (from-blackboard 
   blackboard [robot target]
   (if-not (nil? target)
     (let [self-bearing (.getHeadingRadians robot)
	   target-bearing (Math/toRadians (:bearing target))
	   gun-heading (.getGunHeadingRadians robot)
	   abs-bearing (+ self-bearing target-bearing)
	   sin (Math/sin (- target-bearing abs-bearing))
	   vel (/ (* (:velocity target) sin) 13)
	   angle (Utils/normalRelativeAngle 
		  (+ (- abs-bearing gun-heading) vel))]
       (.setTurnGunRight robot (Math/toDegrees angle))
       (execute robot)))))

(defn fire [blackboard]
  (from-blackboard blackboard [robot]
		   (.fire robot 3) true))

(defn -init []
  [[] (ref {:enemies {}})])

(defn setup [robot]
  (.setColors robot Color/RED Color/WHITE Color/RED)
  (dosync (alter (.blackboard robot) assoc :robot robot)))

(defn -run [robot]
  (setup robot)
  (let [tree (load-tree "/Users/nakkaya/Desktop/robocode/tank.bt" 
			(.blackboard robot))]
    (forever (exec tree))))

(defn -onScannedRobot [robot event]
  (let [distance (.getDistance event)
        name (.getName event)
        bearing (.getBearing event)
	velocity (.getVelocity event)
  	target {:distance distance :bearing bearing :velocity velocity}]
    (dosync (alter (.blackboard robot) assoc-in [:enemies name] target))))
