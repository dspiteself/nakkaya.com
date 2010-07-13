(ns gez
  (:use [alter-ego.core])
  (:import (java.awt Color)
	   (robocode.util Utils))
  (:gen-class :extends robocode.AdvancedRobot :init init :state board))

(def *safety* 200)

(defn debug [s]
  (.println System/out s))

(defn execute [robot]
  (.execute robot) true)

(defn scan [blackboard]
  (from-blackboard blackboard [robot] 
		   (.setTurnRadarLeft robot 360)
		   (execute robot)))

(defn enemy-found? [blackboard]
  (from-blackboard blackboard [enemies] 
		   (not (empty? enemies))))

(defn next-target [enemies dead]
  (let [alive (filter #(not (contains? dead (key %))) enemies)] 
    (first (first (sort-by #(:distance (val %)) alive)))))

(defn select-target [blackboard]
  (from-blackboard blackboard [enemies dead]
		   (dosync (alter blackboard assoc 
				  :target (next-target enemies dead)))))

(defn face-target [blackboard]
  (from-blackboard blackboard [robot target enemies]
		   (let [bearing (:bearing (enemies target))] 
		     (.setTurnRight robot bearing)
		     (execute robot))))

(defn face-sideway [blackboard]
  (from-blackboard blackboard [robot target enemies]
		   (let [bearing (:bearing (enemies target))] 
		     (.setTurnRight robot (+ 90 bearing))
		     (execute robot))))

(defn ahead [blackboard]
  (from-blackboard blackboard [robot]
		   (.setAhead robot 100)
		   (execute robot)))

(defn back [blackboard]
  (from-blackboard blackboard [robot]
		   (.setAhead robot -100)
		   (execute robot)))

(defn in-range? [blackboard]
  (from-blackboard blackboard [robot target enemies]
		   (let [distance (:distance (enemies target))]
		     (< distance *safety*))))

(defn lock-gun [blackboard]
  (from-blackboard 
   blackboard [robot target enemies]
   (let [target (enemies target)
	 self-bearing (.getHeadingRadians robot)
	 target-bearing (Math/toRadians (:bearing target))
	 gun-heading (.getGunHeadingRadians robot)
	 abs-bearing (+ self-bearing target-bearing)
	 sin (Math/sin (- target-bearing abs-bearing))
	 vel (/ (* (:velocity target) sin) 13)
	 angle (Utils/normalRelativeAngle 
		(+ (- abs-bearing gun-heading) vel))]
     (.setTurnGunRight robot (Math/toDegrees angle))
     (execute robot))))

(defn fire [blackboard]
  (from-blackboard blackboard [robot]
		   (let [energy (.getEnergy robot)] 
		     (cond (> energy 50) (.setFire robot 3)
			   (> energy 20) (.setFire robot 2)
			   :default (.setFire robot 1)))
		   (execute robot)))

(defn too-close-to-wall? [blackboard]
  (from-blackboard blackboard [robot]
		   (let [margin 50 x (.getX robot) y (.getY robot)
			 w (.getBattleFieldWidth robot) 
			 h (.getBattleFieldHeight robot)]
		     (or (<= x margin) (>= x (- w margin)) ;;left right
			 (<= y margin) (>= y (- h margin)))))) ;;bottom top

(defn target-not-dead? [blackboard]
  (from-blackboard blackboard [dead target]
		   (if (nil? target)
		     false (not (contains? dead target)))))

(defn -init []
  [[] (ref {:enemies {} :dead #{} :scanned [0 0]})])

(defn setup [robot]
  (doto robot
    (.setColors Color/RED Color/WHITE Color/RED)
    (.setAdjustGunForRobotTurn true)
    (.setAdjustRadarForGunTurn true)
    (.setAdjustRadarForRobotTurn true))
  (dosync (alter (.board robot) assoc :robot robot)))

(defn -run [robot]
  (setup robot)
  (exec (load-tree "/Users/nakkaya/Desktop/engine/gez.bt" (.board robot))))

(defn -onPaint [robot g]
  (let [[x y] (:scanned @(.board robot))] 
    (.setColor g (java.awt.Color. 0xff 0x00 0x00 0x80))
    (.fillRect g (- x 20) (- y 20) 40 40)))

(defn -onRobotDeath [robot event]
  (dosync (alter (.board robot) assoc 
		 :dead (conj (:dead @(.board robot)) (.getName event)))))

(defn -onScannedRobot [robot event]
  (let [distance (.getDistance event)
        name (.getName event)
        bearing (.getBearing event)
	velocity (.getVelocity event)
	distance (.getDistance event)
  	target {:distance distance :bearing bearing :velocity velocity}
	heading (.getHeading robot)
	angle (Math/toRadians (mod (+ heading bearing) 360))
	x (+ (.getX robot) (* (Math/sin angle) distance))
	y (+ (.getY robot) (* (Math/cos angle) distance))]
    (dosync (alter (.board robot) assoc-in [:enemies name] target)
	    (alter (.board robot) assoc :scanned [x y]))))
