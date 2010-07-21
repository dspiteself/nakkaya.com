---
title: Physics with Clojure
tags: clojure ode4j
---

A brief demonstration of simulating mechanical things in Clojure using
ODE4J,

     (defn world [g]
       (let [[x y z] g] 
         (doto (OdeHelper/createWorld)
           (.setGravity x y z))))

We begin by creating a world object which will hold all rigid bodies and
joints and set the gravitational force that will act on our objects.

     (defn body [world [x y z]]
       (let [body (OdeHelper/createBody world)
             mass (doto (OdeHelper/createMass)
                    (.setSphere 2500 0.05))] 
         (doto body
           (.setMass mass)
           (.setPosition x y z))))

For each object we want to simulate we create a body and set the
parameters associated with it, i.e its shape and mass, for this simple
example we create a body that behaves like a sphere with a density of
2500 and a radius of 0.05,

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

Next we create the simulation environment we will be using, we create two
bodies and two joints forming two pendulums connected to each other,
bottom joint is a ball joint which allows the body to move freely, upper
joint is a hinge joint which is a motor driven joint that rotates
around Z axis with an initial angular speed of 0 and 30 units of maximum
force.

     (defn bang-bang [joint]
       (if (> (.getAngle joint) 2.5)
         (.setParamVel joint -0.1)
         (.setParamVel joint 2)))

In order to keep the pendulum at desired angle, we use a type of
controller called a [bang bang
controller](http://en.wikipedia.org/wiki/Bang%E2%80%93bang_control)
(on-off controller),

     (defn bang-bang [joint]
       (if (> (.getAngle joint) 2.5)
         (.setParamVel joint -0.1)
         (.setParamVel joint 2)))

All thats left to do is to paint the bodies to visualize the motion,
on every tick we repaint the panel, calculate the velocity needed and
let the engine step 0.05 seconds,

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

![ode4j bang-bang pendulum](/images/post/ode4j-pendulum.png)

Files,

 - [bang-bang.clj](/code/clojure/bang-bang.clj)
