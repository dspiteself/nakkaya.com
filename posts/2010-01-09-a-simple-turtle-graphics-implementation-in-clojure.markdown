---
title: A Simple Turtle Graphics Implementation in Clojure
tags: clojure graphics
---

Turtle graphics is a popular way for introducing programming to kids. It
was part of the original Logo programming language developed by Wally
Feurzig and Seymour Papert in 1966. Turtle, originally a robotic
creature moves on the floor. It takes commands relative to its own
position, such as "move forward 10 steps" and "turn left 90
degrees". Turtle also has a pen which may be lowered to the floor so
that a trace is left of where it has traveled.

[turtle.clj](/code/clojure/turtle.clj)

     (defn fib [turtle depth]
       (forward turtle 30)
       (if (> depth 2)
         (do 
           (left turtle 15)
           (fib turtle (- depth 1))
           (right turtle 30)
           (fib turtle (- depth 2))
           (left turtle 15)))
       (back turtle 30))

     (let [turtle (turtle 400 400)]
       (pen-up turtle)
       (go turtle 0 -100)
       (pen-down turtle)
       (fib turtle 10)
       (show turtle))

![fern](/images/post/fib.png)

     (defn fern [turtle size]
       (if (> size 4)
         (do 
           (forward turtle  (/ size 25))
           (left turtle 90) (fern turtle (* size 0.3))
           (right turtle 90)
           (right turtle 90) (fern turtle (* size 0.3))
           (left turtle 90)  (fern turtle (* size 0.85))
           (back turtle (/ size 25)))))

     (let [turtle (turtle 400 400)] 
       (pen-up turtle)
       (go turtle 0 -200)
       (pen-down turtle)
       (pen-color turtle Color/green)
       (fern turtle 1500)
       (write turtle "test.png"))

![tree](/images/post/fern.png)
