---
title: Time-lapse photography with Clojure
tags: clojure gphoto jna
---

Over the weekend I was experimenting with
[gPhoto](http://www.gphoto.org/), which is a photo tools suite,
*libgphoto2* library it provides allows other frontends to be written
for it, the idea here was to see if I can control a camera from Clojure
because a drone without a camera is useless.

<p id='timelapse-preview'>You should see a video here...</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','320','240','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/gphoto-time-lapse.mp4');
	s1.write('timelapse-preview');
</script>

This time instead of calling *libgphoto2* functions directly, I used a
custom library to interact with *libgphoto2* that way I only return/pass
stuff that I am interested in, unlike my previous example on
[JNA](http://nakkaya.com/2009/11/16/java-native-access-from-clojure/)
which required a lot of byte counting to extract the information I
needed.

For the *ptp_lib*, I basically took
[examples](http://www.google.com/codesearch/p?hl=en#_cGRBGQmfbU/trunk/libgphoto2/examples/sample-capture.c&q=gphoto2%20sample%20capture&d=3)
from gPhoto and chopped them into small functions to control various
aspects of the camera,

     void* ptp_init()
     int extend_lens(ptp_handle* handle)
     int retract_lens(ptp_handle* handle)
     int preview(ptp_handle* handle, char *fn)
     int ptp_exit(ptp_handle* handle)

On the Clojure side,

     (defn ptp [func ret & args]
       (let [f (Function/getFunction "ptp_lib" (name func))]
         (.invoke f ret (to-array args))))

     (let [camera (delay (ptp :ptp_init Pointer))
           index (atom 1)
           running (atom true)]
  
       (defn start []
         (let [out-dir (file "prevs")]
           (if (not (.exists out-dir))
             (.mkdir out-dir))
           (ptp :extend_lens Integer @camera)
           (.start (Thread. (fn []
                              (while @running
                                (let [f (str "prevs/preview-" @index ".jpg")]
                                  (ptp :preview Integer @camera f))
                                (swap! index inc)
                                (Thread/sleep 15000)))))))
  
       (defn stop []
         (swap! running not)
         (ptp :retract_lens Integer @camera)
         (ptp :ptp_exit Integer @camera)))

We initialize the camera, extend the lens and take one photo every 15
seconds. When done ffmpeg can turn the batch of photos in to a movie,

    ffmpeg -f image2 -r 25 -i prevs/preview-%d.jpg \ 
    -vcodec libx264 -vpre hq -crf 22 video.mp4

In order to play with the example, you need to compile *ptp_lib.c* using
the instructions on the top of the file and place it in your working
directory.

You also need to check if your camera is
[supported](http://www.gphoto.org/doc/remote/) and if you are on Mac OS
X you need to kill the PTP daemon before running the code.

Download,

 - [ptp.clj](/code/clojure/gphoto-time-lapse/ptp.clj)
 - [ptp_lib.c](/code/clojure/gphoto-time-lapse/ptp_lib.c)
