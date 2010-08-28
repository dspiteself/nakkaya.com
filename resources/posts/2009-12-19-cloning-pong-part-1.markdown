---
title: Cloning Pong Part 1
tags: clojure arduino pong
---

This will be a two part write up, part one will cover building a simple
[pong](http://en.wikipedia.org/wiki/Pong) game in Clojure, part two will
cover, interfacing it with [Arduino](http://www.arduino.cc/), so that we
can control the game using knobs like the original.

If all you want it to play some Pong, source code for the game can be found
[here](/code/clojure/pong.clj). I wanted to keep the code simple so it
has some flaws such as the ball only goes in 45 degree angles and you can
only hit the ball with the front of the pad. If you want a perfect clone,
flaws can be fixed at the expense of making the code a little bit more
complex.

Two references are used in the game, one for the ball and one for the
players,

    {:x 400, :y 200, :size 8, :dir {:x -1, :y 1}}

Reference for the ball contains it's x and y coordinates on the game
board and the direction it is travelling.

    {:1 {:x 10 :y 200 :src 0} 
     :2 {:x 400 :y 300 :src 0}}

Reference for the players contains positions of the pads and keeps track
of the scores. Thats all the mutable state the game has.

Lets go over the important bits of the code,

     (defn move-player [p dir player]
       (let [x (if (= p :1) (:x (:1 @player)) (:x (:2 @player)))
             y (if (= p :1) (:y (:1 @player)) (:y (:2 @player)))
             delta (* 3 (if (= dir :up) (:up step) (:down step)))]
         (dosync (alter player merge 
                        {p {:x x :y (- y delta) :src (:src (p @player))}}))))

     (move-player :1 :up 
                  (ref {:1 {:x 10 :y 200 :src 0} :2 {:x 400 :y 300 :src 0}}))
     pong=> {:1 {:x 10, :y 185, :src 0}, :2 {:x 400, :y 300, :src 0}}

When move-player is called, depending on the player and direction value
passed, it will calculate the new x and y coordinates and update the
player reference.

     (defn place-ball []
       (let [x (if (= (rand-int 2) 0) -1 1)
             y (if (= (rand-int 2) 0) -1 1)]
         {:x (int (/ (:w board-size) 2)) 
          :y (int (/ (:h board-size) 2)) :size 8 :dir {:x x :y y}}))

Every time a player scores, position of the ball is reset, it will be
placed in the middle of the board with a random direction.

     (defn move-ball [ball]
       (let [x (cond (= 1  (:x (:dir @ball))) (+ (:x @ball) (:right step))
                     (= -1 (:x (:dir @ball))) (+ (:x @ball) (:left step))
                     :else (:x @ball))
             y (cond (= 1  (:y (:dir @ball))) (+ (:y @ball) (:up step))
                     (= -1 (:y (:dir @ball))) (+ (:y @ball) (:down step))
                     :else (:y @ball))]
         (dosync (alter ball merge {:x x :y y}))))

Before every repaint of the game board, we move the ball in the
direction it is going and update its reference.

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

Here we check for collisions, if the ball hits the right wall, we
increment player one's score and reset the ball, if the ball hits the
left wall we increment player two's score and reset the ball, if the
ball hits the upper or the lower wall, we change it's y direction which
will cause it to bounce from the wall.

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

Besides walls, ball collides with the player pads, when the ball
collides, with a player we keep it's y direction but change it's x
direction to send it in the opposite direction.

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

paintComponent just draws the board nothing too fancy, actionPerformed
however contains the game logic, every 50 millisecond we move the ball,
check for a wall collision, check for a player collision then repaint the
game board. All that's needed now, is to put the game board in a frame and
start it's timer.

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

![pong](/images/post/pong.png)
