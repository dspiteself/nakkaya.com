---
title: Fractals in Clojure - Sierpinski Triangle 
tags: clojure fractal
---

This fractal is called the Sierpinski triangle, after the Polish
mathematician Waclaw Sierpinski.

####  Construction

In order to generate it we'll use the following algorithm,

 - We pick a triangle on the surface.
 - We shrink the triangle to half the height and width, make three
   copies, and position the three triangles so that each triangle
   touches the two other triangles at a corner.
 - We repeat the above step until required depth is reached.

#### Code

    (defstruct point :x :y)
    (defstruct triangle :left :rigth :bottom)

We need two structures for representing a point on the canvas and the
triangle we are drawing.

    (defn midpoints [trig]
      (struct triangle 
              (struct point
                      (int (/ (+ (:x (:bottom trig)) (:x (:left trig))) 2 ))
                      (int (/ (+ (:y (:bottom trig)) (:y (:left trig))) 2 )))
              (struct point
                      (int (/ (+ (:x (:rigth trig)) (:x (:bottom trig))) 2 ))
                      (int (/ (+ (:y (:rigth trig)) (:y (:bottom trig))) 2 )))
              (struct point
                      (int (/ (+ (:x (:left trig)) (:x (:rigth trig))) 2 ))
                      (int (/ (+ (:y (:left trig)) (:y (:rigth trig))) 2 )))))

Given a triangle this function will return a new triangle thats half the
original triangle.

    (defn create-triangles [trig step depth g]
      (paint-triangle g trig)
      (let  [points (midpoints trig) 
             left   (struct 
                     triangle (:left trig) (:left points) (:bottom points))
             rigth  (struct 
                     triangle (:rigth trig) (:rigth points) (:bottom points))
             top    (struct 
                     triangle (:bottom trig) (:rigth points) (:left points)) ]
        (if (< step depth )
          (do (create-triangles left  (inc step) depth g)
              (create-triangles rigth (inc step) depth g)
              (create-triangles top   (inc step) depth g)))))

create-triangles is a recursive function that will call it self until
required depth is reached. It first draws the given triangle then
calculate 3 smaller triangles that will be placed inside the parent
triangle.

![Sierpinski](/images/post/sierpinski1.png)

![Sierpinski](/images/post/sierpinski2.png)

Download [code](/code/clojure/sierpinski.clj)
