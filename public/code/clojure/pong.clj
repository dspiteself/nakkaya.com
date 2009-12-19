(ns pong
  (:import (javax.swing JFrame JPanel Timer)
	   (java.awt Color BasicStroke Font Toolkit)
	   (java.awt.event KeyListener KeyEvent ActionListener)))

(def board-size {:w 800 :h 400})
(def pad-size {:w 5 :h 25})
(def step {:right 5 :left -5 :up 5 :down -5})

(defn beep []
  (.beep (. Toolkit getDefaultToolkit)))

(defn move-player [p dir player]
  (let [x (if (= p :1) (:x (:1 @player)) (:x (:2 @player)))
	y (if (= p :1) (:y (:1 @player)) (:y (:2 @player)))
	delta (* 3 (if (= dir :up) (:up step) (:down step)))]
    (dosync (alter player merge 
		   {p {:x x :y (- y delta) :src (:src (p @player))}}))))

(defn place-ball []
  (let [x (if (= (rand-int 2) 0) -1 1)
	y (if (= (rand-int 2) 0) -1 1)]
    {:x (int (/ (:w board-size) 2)) 
     :y (int (/ (:h board-size) 2)) :size 8 :dir {:x x :y y}}))

(defn move-ball [ball]
  (let [x (cond (= 1  (:x (:dir @ball))) (+ (:x @ball) (:right step))
		(= -1 (:x (:dir @ball))) (+ (:x @ball) (:left step))
		:else (:x @ball))
	y (cond (= 1  (:y (:dir @ball))) (+ (:y @ball) (:up step))
		(= -1 (:y (:dir @ball))) (+ (:y @ball) (:down step))
		:else (:y @ball))]
    (dosync (alter ball merge {:x x :y y}))))

(defn inc-score [p player]
  (beep)
  (dosync (alter player merge {p {:x (:x (p @player)) :y (:y (p @player))
				  :src (inc (:src (p @player)))}})))

(defn wall-collision [ball player]
  (cond
   (<= (:x @ball) 0) (do (inc-score :1 player)
			 (dosync (ref-set ball (place-ball))))
   (>= (:x @ball) (:w board-size)) (do (inc-score :2 player)
				       (dosync (ref-set ball (place-ball))))
   :else
   (let [dir-x (:x (:dir @ball))
	 dir-y (cond (<= (:y @ball) 0) 1
		     (>= (:y @ball) (:h board-size)) -1
		     :else (:y (:dir @ball)))]
     (dosync (alter ball merge {:dir {:x dir-x :y dir-y}})))))

(defn player-collision [ball player]
  ;;p1 collision
  (if (and (=  (:x @ball) (+ (:x (:1 @player)) (:w pad-size)))
	   (>= (:y @ball) (:y (:1 @player)))
	   (<= (:y @ball) (+ (:y (:1 @player)) (:h pad-size))))
    (do (dosync (alter ball merge {:dir {:x 1 :y (:y (:dir @ball))}}))
	(beep)))
  ;;p2 collision
  (if (and (=  (:x @ball) (:x (:2 @player)))
	   (>= (:y @ball) (:y (:2 @player)))
	   (<= (:y @ball) (+ (:y (:2 @player)) (:h pad-size)))) 
    (do (dosync (alter ball merge {:dir {:x -1 :y (:y (:dir @ball))}}))
	(beep))))

(defn key-listener [board player]
  (let [move (fn[k]
	       (let [key (.getKeyCode k)]
		 (cond 
		  (= key KeyEvent/VK_W) (move-player :1 :up player)
		  (= key KeyEvent/VK_S) (move-player :1 :down player)
		  (= key KeyEvent/VK_UP) (move-player :2 :up player)
		  (= key KeyEvent/VK_DOWN) (move-player :2 :down player))))]
    (proxy [java.awt.event.KeyListener] [] 
      (keyPressed [e]  (move e))
      (keyReleased [e] (move e))
      (keyTyped [e] (move e)))))

(defn board [player ball]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent
     [g]
     (proxy-super setOpaque false)
     (proxy-super paintComponent g)
     (let [quarter (int (/ (:w board-size) 4))] 
       (doto g
	 (.setColor Color/black)
	 (.fillRect 0 0 (:w board-size) (:h board-size))
	 ;;ball
	 (.setColor Color/white)
	 (.fillOval (:x @ball) (:y @ball) (:size @ball) (:size @ball))
	 ;;pads
	 (.fillRect (:x (:1 @player)) (:y (:1 @player)) 
		    (:w pad-size) (:h pad-size))
	 (.fillRect (:x (:2 @player)) (:y (:2 @player))
		    (:w pad-size) (:h pad-size))
	 ;;scores
	 (.setFont (Font. "arial" Font/PLAIN 40))
	 (.drawString (str (:src (:1 @player))) quarter 50)
	 (.drawString (str (:src (:2 @player))) (* quarter 3) 50)
	 ;;dashed line
	 (.setStroke (BasicStroke. 3 BasicStroke/CAP_BUTT 
				   BasicStroke/JOIN_BEVEL 0 
				   (float-array [12 12]) 0))
	 (.drawLine (int (/ (:w board-size) 2)) 0
		    (int (/ (:w board-size) 2)) (:h board-size)))))
    (actionPerformed 
     [e] 
     (move-ball ball)
     (wall-collision ball player)
     (player-collision ball player)
     (.repaint this))))

(defn pong []
  (let [ball   (ref (place-ball))
	mid-y (int (/ (:h board-size) 2))
	player (ref {:1 {:x 10 :y mid-y :src 0} 
		     :2 {:x (- (:w board-size) 20) :y mid-y :src 0}})
	brd    (board player ball)
	timer  (Timer. 50 brd)]
    (doto (JFrame.)
      (.add brd)
      (.setTitle "Pong!")
      ;;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
      (.setLocationRelativeTo nil)
      (.setAlwaysOnTop true)
      (.setResizable false)
      (.setSize (java.awt.Dimension. (:w board-size) 
				     (+ 22 (:h board-size))))
      (.addKeyListener (key-listener brd player))
      (.setVisible true))
    (.start timer)))

;;(pong)
