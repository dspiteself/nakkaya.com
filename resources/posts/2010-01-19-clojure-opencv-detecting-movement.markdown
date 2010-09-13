---
title: Clojure OpenCV Detecting Movement
tags: clojure opencv
---

I have been experimenting with OpenCV to use webcam as a control
device. I've put together a sample application which uses difference
between frames to detect movement and perform collision detection with
objects on the screen.

<p id='preview'>Player</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','650','406','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/opencv-motion-detection.mp4');
	s1.write('preview');
</script>

[movement-detect.clj](/code/clojure/movement-detect.clj)

     (defn create-circle []
       (let [x (rand-int (- width circle-radius))
             y (rand-int (- height circle-radius))]
         {:x x :y y :area (for [y (range y (+ y circle-radius))
                                x (range x (+ x circle-radius))] [x y])}))

Since we will be moving circles around the screen, we begin by defining
a circle, x and y represent the upper left corner of the circle, area
contains a list of coordinates that circle occupies on the screen.

     (defn capture-image [vis]
       (.read vis)
       (let [raw (.pixels vis)] 
         (doto vis
           (.absDiff)
           (.convert OpenCV/GRAY)
           (.blur OpenCV/BLUR 3)
           (.threshold 20)
           (.remember))
         {:raw raw :diff (.pixels vis)}))

We capture a frame from the camera, save a copy of the image as raw,
calculate the difference between the current image and the previous one,
keeping only parts of the image where there is movement, convert it to
grey scale and apply blur on it to remove camera noise, after running it
through the threshold filter resulting image will contain white where
there is movement black everywhere else.

![opencv clojure motion detection](/images/post/opencv-motion.png)

     (defn white-pixel-count [circle pixels]
       (reduce (fn[h v]
                 (let [x (first v) y (second v)
                       pix (nth pixels (+ x (* y width)))
                       blue (bit-and pix 0x000000ff)]
                   (if (= blue 255) (inc h) h))) 0 (:area circle)))

     (defn collision? [circle pixels]
       (let [white (white-pixel-count circle pixels)] 
         (cond (zero? white) false
               (> (/ 1 (/ (count (:area circle)) white)) 0.2) true
               :else false)))

Detecting collision works by counting the number of white pixels under
the circle, if more than 20% of the pixels under the circle contains
white, it indicates movement.

     (defn validate-circles [circles pixels]
       (reduce (fn[h c]
                 (if (collision? c pixels)
                   (conj h (create-circle))
                   (conj h c))) [] @circles))

     (defn capture-action [vis panel image circles]
       (proxy [ActionListener] []
         (actionPerformed
          [e]
          (let [capture (capture-image vis)]
            (dosync (ref-set image capture)
                    (ref-set circles (validate-circles circles (:diff capture)))))
          (.repaint panel))))

With every tick of the timer, we'll iterate over the circles on the
screen, checking for collision, if there is collision replace that
circle with a new one and repaint the panel to reflect changes.

This covers the meat of the code, I have skipped some boiler plate code
such as setting up OpenCV or building images which I already covered
[here](/2010/01/12/fun-with-clojure-opencv-and-face-detection/). If you
end up making anything cool with it, please leave a comment with a
link. I'd love to see it.
