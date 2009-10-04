---
title: Fractals in Clojure - Buddhabrot Fractal
layout: post
tags: clojure fractal
---

Buddhabrot is a special rendering of the
[Mandelbrot](http://en.wikipedia.org/wiki/Mandelbrot_set)
fractal. Rendering technique we will use is developed by [Melinda
Green](http://www.superliminal.com/fractals/bbrot/bbrot.htm).

![buddhabrot-400mil400ite](/images/post/buddhabrot-400mil400ite.png)

First we create a 2D array that will map to the pixel's on the
screen. Next we start picking random points on the image and apply the
mandelbrot formula. We iterate through the formula for points which do
escape within the chosen number of iterations.


To get started we need to get some utility functions out of the way, at
first i used the complex number library in clojure-contrib but for some
reason they were taking a long time to compute. So i wrote my own.

    (defstruct complex :real :imag)

    (defn add
      "Complex addition"
      [ c1 c2 ] 
       (struct complex 
              (+ (:real c1) (:real c2))
              (+ (:imag c1) (:imag c2))))

    (defn multiply
      "Complex Multipication"
      [ c1 c2 ] 
      (struct complex 
              (- (* (:real c1) (:real c2)) (* (:imag c1) (:imag c2)))
              (+ (* (:real c1) (:imag c2)) (* (:imag c1) (:real c2)))))

    (defn abs
      "Complex Absulute Value"
      [ complex ]
      (Math/sqrt 
       (+ (* (:real complex) (:real complex) ) 
          (* (:imag complex) (:imag complex)))))


For each point we pick on the screen we need to calculate how the point
escapes.


    (defn calc-path
      [ x y max-iterations ] 
      (let  [ c (struct complex x y) ]
        (loop [z c 
               path [  ]
               iterations 0 ]
          (if  (> iterations max-iterations)
            []
            (if (> (abs z ) 2.0 ) 
              (conj path z)
              (recur (add c (multiply z z)) (conj path z) (inc iterations)))))))


If the point escapes with in the chosen number of iterations we get a
list of points. For each point we get, we increment a counter in the
buffer. In the end we color the fractal based on the number of
iterations that passed through that pixel.


Since drawing buddhabrot is an expensive operation and takes a long time to
render good looking pictures. Calculations are done on a separate thread
generate function will run indefinitely, you can check picture quality
using the draw function. Because there will be no output you need to run
the script in REPL and call draw to look at it.

    (defn generate [fractal]
      (let  [buffer (:buffer fractal)
             rand   (new Random)]
        (doseq [point (iterate inc 1)]
          (let  [x    (- (* 6 (.nextDouble rand)) 3) 
                 y    (- (* 6 (.nextDouble rand)) 3)
                 path   (calc-path x y (:iteration fractal))]

            (if (= (mod point 1000000) 0)
              (println "Point: " point))

            (doseq [p path] (buffer-set fractal p)))) ))

    (defn start [fractal]
      (.start (Thread. (proxy [Runnable] [] (run [] (generate fractal)))))) 

For coloring the image we use the same algorithm that we used to draw
the mandelbrot set,


    (defn calc-pixel-color
      [ iteration max-iterations ] 
      (let  [ gray (int (/ (* iteration 255) max-iterations )) 
              r    gray
              g    (Math/min (int ( / (* 5 ( * gray gray)) 255)) 255)
              b    (Math/min (int (+ 40 ( / (* 5 (* gray gray)) 255))) 255) ]
        (try (new Color r g b ) (catch Exception e (new Color 0 0 0))) ))


We iterate through the array paint everything on an BufferedImage then
draw that on a JLabel.

    (defn paint-canvas [fractal graphics] 
      (let  [buffer (:buffer fractal)
             biggest  (apply max (map #(apply max %) buffer)) ]
        (doseq [y (range (:height fractal))
                x (range (:width fractal)) ]
     
          (if  (> (aget buffer y x) 0 )
            (do
              (.setColor graphics (calc-pixel-color (aget buffer y x) biggest ))
              (.drawLine graphics x y x y ))))))


    (defn draw [fractal]
      (let [frame  (new JFrame)
            image  (new BufferedImage 
                        (:width fractal) (:height fractal) 
                        BufferedImage/TYPE_INT_RGB)
            canvas (proxy [JLabel] [] (paint [g] (.drawImage g image 0 0 this)))
            graphics (.createGraphics image)]

        (paint-canvas fractal graphics)

        (.add frame canvas)
        (.setSize frame (new Dimension (:width fractal) (:height fractal)))
        (.show frame)))


To play with the script, define a fractal

    (def fractal {:buffer (make-array Integer/TYPE 800 800)
                  :width 800 :height 800 :iteration 50})

Start calculations,

    (start fractal)


Check the result at intervals until you are satisfied.
    
    (draw fractal)


Below are the some shots on how the image progresses,

#### 10 Million Points

![buddhabrot-10mil](/images/post/buddhabrot-10mil.png)

#### 30 Million Points

![buddhabrot-30mil](/images/post/buddhabrot-30mil.png)

#### 60 Million Points

![buddhabrot-60mil](/images/post/buddhabrot-60mil.png)

#### 400 Million Points 400 Iterations

![buddhabrot-400mil400ite](/images/post/buddhabrot-400mil400ite.png)
