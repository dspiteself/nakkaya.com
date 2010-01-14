---
title: Hilbert Curve
tags: clojure fractal graphics
---

The other day, I came across
[this](http://www.isi.edu/ant/address/index.html), a visualization of
the Internet address space. Where they ping all the IP addresses in the
IP v4 space and plot a table depending on the response from the server,
they draw the graph using a fractal called a [Hilbert
Curve](http://en.wikipedia.org/wiki/Hilbert_curve), following uses my
[turtle
graphics](/2010/01/09/a-simple-turtle-graphics-implementation-in-clojure/)
implementation to draw the curve.

![hilbert curve](/images/post/hilbert.png)

     (ns hilbert
       (:use :reload-all turtle))

     (def size 10)
     (def width 330)
     (def height 330)

     (defn hilbert [turtle level angle]
       (if (> level 0)
         (do 
           (right turtle angle)
           (hilbert turtle (- level 1) (- angle))
           (forward turtle size)
           (left turtle angle)
           (hilbert turtle (- level 1) angle)
           (forward turtle size)
           (hilbert turtle (- level 1) angle)
           (left turtle angle)
           (forward turtle size)
           (hilbert turtle (- level 1) (- angle))
           (right turtle angle))))

     (let [turtle (turtle width height)]
       (pen-up turtle)
       (go turtle (- 10 (/ width 2)) (- 10 (/ height 2)))
       (pen-down turtle)
       (hilbert turtle 5 90)
       (show turtle))

