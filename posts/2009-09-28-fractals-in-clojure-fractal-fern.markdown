---
title: Fractals in Clojure - Fractal Fern
layout: post
tags: clojure fractal
---

In this post I'm going to show how to draw a type of
[Fractal](http://en.wikipedia.org/wiki/Fractal) called
[Barnsley's](http://mathworld.wolfram.com/BarnsleysFern.html) fern.

Basically it goes like this,

 - We start at a point, x=0  y=1 in this case
 - Paint that point on the canvas.
 - Run that point through the transformation function to get the next point.
 - Goto step 2 until desired number of points are painted on the canvas.

This example uses the transformation functions
[from](http://en.wikipedia.org/wiki/Iterated_function_system#Example:_a_fractal_.22fern.22)
the Wikipedia article on [Iterated function
systems](http://en.wikipedia.org/wiki/Iterated_function_system).

#### Transformation


    (defn transform-one [ target ] 
      (struct <point> 0 (* 0.16 (:y target))))
    (defn transform-two [ target ] 
      (struct <point> 
	      (- (* 0.2  (:x target)) (* 0.26 (:y target))) 
	      (+ (* 0.23 (:x target)) (* 0.22 (:y target)))))
    (defn transform-three [ target ] 
      (struct <point>  
	      (+ (* -0.15 (:x target)) (* 0.28 (:y target)))  
	      (+ (* 0.26  (:x target)) (* 0.24 (:y target)) 0.44)  ))
    (defn transform-four [ target ] 
      (struct <point> 
	      (+ (* 0.85   (:x target)) (* 0.04 (:y target)))
	      (+ (* -0.004 (:x target)) (* 0.85 (:y target)) 1.6)  ))

    (defn transform 
      "Transform point accourding to the percentage."
      [ target ] 
      (let  [ random (new Random) 
	      percentage (.nextInt random 100) ] 
        (cond 
         (<= percentage 1 ) (transform-one target)
         (<= percentage 7 ) (transform-two target)
         (<= percentage 14 ) (transform-three target)
         (<= percentage 100 ) (transform-four target) )))


We have four transformations, each transformation is selected at random.

 - Transformation one is selected 1% of the time.
 - Transformation two is selected 7% of the time.
 - Transformation three is selected 7% of the time.
 - Transformation four is selected 85% of the time.

Transformation one draws the stem. Transformation two draws the bottom
frond on the left. Transformation three draws the bottom frond on the
right. Transformation four generates successive copies of the stem and
bottom fronds to make the complete fern.

![Fractal Fern](/images/post/150px-Fractal-fern-explained.png)

We can call transform repeatedly to get points to draw on the canvas. We
can use loop for this but there is better way.

    (defn draw-fern [ width height max-points graphics ] 
      (let [ coords  (take max-points (iterate transform (struct <point> 0 1)))]
        (doseq [coord coords]
          (paint-point width height coord graphics )) ))

We use iterate,

    user=> (doc iterate)
    -------------------------
    clojure.core/iterate
    ([f x])
      Returns a lazy sequence of x, (f x), (f (f x)) etc. f must be free
    of side-effects

Iterate takes a function and a initial value and returns an infinite
sequence of coordinates.

    ({:x 0, :y 1} {:x 0.28, :y 0.6799999999999999} {:x 0.2652, :y 2.17688} ...

No looping required. It applies the result of each function to the
function so that we get a vector of values. Then we take the number of
points we want from the sequence.

    (defn draw [width height points]
      (let [frame  (new JFrame)
	    image  (new BufferedImage width height BufferedImage/TYPE_INT_RGB )
	    canvas (proxy [JLabel] []
		     (paint [g]			  
			    (.drawImage g image 0 0 this) ))
	    graphics (.createGraphics image)]
        (.setColor graphics Color/green)
        (draw-fern width height points graphics)
        (.add frame canvas)
        (.setSize frame (new Dimension width height))
        (.show frame)))

Next we paint everything on a BufferedImage and paint that on a
JLabel, and voila.

#### 400 by 400 10,000 points
![Fractal Fern](/images/post/fern400x400x10000.png)

#### 400 by 400 100,000 points
![Fractal Fern](/images/post/fern400x400x100000.png)

Download [code](/code/clojure/fern.clj)
