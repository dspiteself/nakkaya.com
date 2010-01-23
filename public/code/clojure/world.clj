(ns world
  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.prxml)
  (:import  (java.net URL)
	    (java.io BufferedReader InputStreamReader)
	    (java.awt Dimension)
	    (gov.nasa.worldwind Configuration WorldWind)
	    (gov.nasa.worldwind.avlist AVKey)
	    (gov.nasa.worldwind.awt WorldWindowGLCanvas)
	    (gov.nasa.worldwind.geom LatLon Position)
	    (gov.nasa.worldwind.view.orbit BasicOrbitView)
	    (gov.nasa.worldwind.layers IconLayer)
	    (gov.nasa.worldwind.render UserFacingIcon)
	    (gov.nasa.worldwind.event SelectEvent)))

(defstruct earth-quake 
  :date :time :latitude :longitude :depth :md :ml :ms :location)

(defn fetch-url[address]
  (let  [url (URL. address)] 
    (with-open [stream (.openStream url)]
      (let  [buf (BufferedReader. 
		  (InputStreamReader. stream "windows-1254" ))]
	(apply str (interleave (line-seq buf) (repeat \newline )))))))

(defn parse [data]
   (map
    #(apply struct earth-quake (re-split #"\s+" % 9))
    (re-split #"\n+" data)))

(defn eartquakes []
  (let  [page (fetch-url "http://www.koeri.boun.edu.tr/scripts/lst9.asp")
	 data (re-find #"(?s)------------    -----------\n(.*?)</pre>" page)]
    (parse (data 1))))

(defn world []
  (Configuration/setValue AVKey/INITIAL_LATITUDE 39.3113)
  (Configuration/setValue AVKey/INITIAL_LONGITUDE 32.8038)
  (Configuration/setValue AVKey/INITIAL_ALTITUDE 1000000)
  (doto (WorldWindowGLCanvas.)
    (.setModel (WorldWind/createConfigurationComponent 
		AVKey/MODEL_CLASS_NAME))))

(defn goto-pos [world lat long elev]
  (let [position (Position. (LatLon/fromDegrees lat long) (* elev 10000))
	view (cast BasicOrbitView (.getView world))]
    (.goTo view position (* elev 10000))))

(defn icon [quake]
  (doto (UserFacingIcon. 
  	 "icon.png" (Position.
  		     (LatLon/fromDegrees (Double. (:latitude quake))
  					 (Double. (:longitude quake))) 0.0))
    (.setToolTipText (apply str (interleave quake (repeat " "))))))

(defn icon-layer [icons]
  (let [layer (IconLayer.)] 
    (doseq [icon icons] 
      (.addIcon layer icon)) layer))

(defn select-listener []
  (proxy [gov.nasa.worldwind.event.SelectListener] [] 
    (selected 
     [e]
     (let [object (.getTopObject e)] 
       (if (= (.getEventAction e) SelectEvent/LEFT_CLICK)
	 (if (instance? UserFacingIcon object)
	   (.setShowToolTip object (not (.isShowToolTip object)))))))))

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

(frame)
