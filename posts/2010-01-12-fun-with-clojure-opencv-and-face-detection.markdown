---
title: Fun with Clojure, OpenCV and Face Detection
tags: clojure opencv
---

I have been meaning to play with
[OpenCV](http://opencv.willowgarage.com/wiki/) for a while now, the
other night, I had some time to kill so I decided to play with
it. OpenCV is a computer vision library originally developed by Intel.
It focuses mainly on real-time image processing. I assumed by now there
are a lot of Java libraries for OpenCV but as it turns out there is only
one, and it is a [Processing](http://processing.org/) library. It works
with Java out of the box but for Clojure it takes a little more effort.

![clojure opencv](/images/post/face-detect.png)

Grab the [OpenCV library](http://ubaa.net/shared/processing/opencv/),
they provide both OpenCV library and Java bindings. Install OpenCV and
copy Java bindings to your extensions folder. OpenCV library has two
constructors,

     OpenCV() 
               Create a new OpenCV instance.
     OpenCV(processing.core.PApplet parent) 
               Create a new OpenCV instance.

First constructor is for Java and second one is for Processing, if you
try to initialize it from Clojure, it will fail trying to locate PApplet
class which is distributed with Processing or Arduino IDEs. Install
either one of them, grab core.jar that comes with it and copy that to
your extensions folder also.

 - [face-detect.clj](/code/clojure/face-detect.clj)

First we need to configure OpenCV object,

     (defn vision []
       (doto (OpenCV.)
         (.capture width height)
         (.cascade OpenCV/CASCADE_FRONTALFACE_ALT)))

We will be capturing from the default webcam and using the FRONTALFACE
description file. You can supply your own for detecting other stuff
besides faces.

     (defn capture-image [vis]
       (.read vis)
       (let [mis (MemoryImageSource. (.width vis) (.height vis)
                                     (.pixels vis) 0 (.width vis))]
         (.createImage (Canvas.) mis)))

Before processing we need to grab a new frame from the camera, we also
build a Image from the data we read to be painted on a component.

      (defn detect-face [vis]
        (.detect vis 1.2 2 OpenCV/HAAR_DO_CANNY_PRUNING 20 20))

Now we are ready to detect object(s) in the current image depending on
the current cascade description. detect will return an array of
rectangles where faces are detected.

     (defn capture-action [vis panel image faces]
       (proxy [ActionListener] []
         (actionPerformed
          [e]
          (dosync (ref-set image (capture-image vis))
                  (ref-set faces (detect-face vis)))
          (.repaint panel))))

     (defn panel [image faces]
       (proxy [JLabel] [] 
         (paint
          [g]
          (.drawImage g @image 0 0 nil)
          (.setColor g Color/red)
          (doseq [square @faces]
            (.drawRect g
                       (.x square) (.y square)
                       (.width square) (.height square))))))

With every tick of the timer, we will grab a new frame from the camera,
detect faces in the image then repaint the panel to reflect changes.

     (defn key-listener [vis timer]
       (proxy [KeyAdapter] [] 
         (keyReleased 
          [e]
          (.stop timer)
          (.dispose vis))))

You need to properly dispose of OpenCV object or bad things will happen,
you are warned. Just listen for a key event, when the event occurs stop
the timer and dispose the OpenCV object.

     (defn main []
       (let [vis   (vision)
             image (ref (capture-image vis))
             faces (ref (detect-face vis))
             panel (panel image faces)
             timer (Timer. frame-rate (capture-action vis panel image faces))]
         (.start timer)
         (doto (JFrame.)
           (.add panel)
           (.addKeyListener (key-listener vis timer))
           (.setSize width height)
           (.show))))

When components assembled and timer started, we start detecting faces.
