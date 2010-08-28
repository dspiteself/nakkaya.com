---
title: Hello World Wind
tags: clojure world-wind
---

World Wind is a [virtual
globe](http://en.wikipedia.org/wiki/Virtual_globe), developed by
[NASA](http://www.nasa.gov/), it gives us the ability to add [Google
Earth](http://earth.google.com/) like functionality to our
applications.

In my previous post [Mashups Using
Clojure](http://nakkaya.com/2009/12/17/mashups-using-clojure/), I
produced a HTML file and used Google Maps to visualize the data, with
world wind we can integrate the map directly into the application.

![world wind](/images/post/world.png)

I am going to be using the same parsing routines that I used for the
[Mashups Using
Clojure](http://nakkaya.com/2009/12/17/mashups-using-clojure/) post so I
am going to skip explaining those and jump directly to World Wind
stuff. 


First you need to get the latest
[SDK](http://builds.worldwind.arc.nasa.gov/download.asp), unzip it and
copy the jar and library files in to your Java extensions folder, if you
put them somewhere else on your classpath don't forget to set your library
path to point to the native libraries.

     (defn world []
       (Configuration/setValue AVKey/INITIAL_LATITUDE 39.3113)
       (Configuration/setValue AVKey/INITIAL_LONGITUDE 32.8038)
       (Configuration/setValue AVKey/INITIAL_ALTITUDE 1000000)
       (doto (WorldWindowGLCanvas.)
         (.setModel (WorldWind/createConfigurationComponent 
                     AVKey/MODEL_CLASS_NAME))))

The component that holds the world is called WorldWindowGLCanvas, you
don't need to set any initial values but you need to set the model.

     (defn goto-pos [world lat long elev]
       (let [position (Position. (LatLon/fromDegrees lat long) (* elev 10000))
             view (cast BasicOrbitView (.getView world))]
         (.goTo view position (* elev 10000))))

If later on you want to change the orientation of the map, fly to a
different location, you can call goTo method of the view but beware, if
you try to use it before the map is shown in a frame it won't work,
configure initial values instead.

     (defn icon [quake]
       (doto (UserFacingIcon. 
              "icon.png" (Position.
                          (LatLon/fromDegrees (Double. (:latitude quake))
                                              (Double. (:longitude quake))) 0.0))
         (.setToolTipText (apply str (interleave quake (repeat " "))))))

In order to mark positions on the map, world wind provides
UserFacingIcon class, it takes an image for the icon and the coordinates
to place the icon.

     (defn icon-layer [icons]
       (let [layer (IconLayer.)] 
         (doseq [icon icons] 
           (.addIcon layer icon)) layer))

After creating the icons you need to put them on a IconLayer,

     (defn icon-layer [icons]
       (let [layer (IconLayer.)] 
         (doseq [icon icons] 
           (.addIcon layer icon)) layer))

By default icons don't work like markers in Google Earth, you can't
click on them, to capture events that are happening on the map, we need
to install a SelectListener and check if the event occurred on a
UserFacingIcon, if so we toggle it's tooltip.

     (defn select-listener []
       (proxy [gov.nasa.worldwind.event.SelectListener] [] 
         (selected 
          [e]
          (let [object (.getTopObject e)] 
            (if (= (.getEventAction e) SelectEvent/LEFT_CLICK)
              (if (instance? UserFacingIcon object)
                (.setShowToolTip object (not (.isShowToolTip object)))))))))

Thats all it takes to create our mashup, we need to create the world,
create a icon for each earthquake, place them on a layer and add it to
the world,

     (defn frame []
       (let [world (world)
             layers (.getLayers (.getModel world))
             earth-quakes (map #(icon %) (eartquakes))]
             (.add layers (icon-layer earth-quakes))
             (.addSelectListener world (select-listener))
             (doto (javax.swing.JFrame.)
               (.add world)
               (.setSize (Dimension. 400 400))
               (.setAlwaysOnTop true)
               (.setVisible true))))

  
Full code listing can be found [here](/code/clojure/world.clj).

ABRKMZZW8ASP
