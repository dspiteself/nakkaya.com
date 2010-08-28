(ns gpx
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml])
  (:use clojure.contrib.zip-filter.xml)
  (:import (java.util ArrayList)
	   (java.awt Dimension)
	   (gov.nasa.worldwind.layers RenderableLayer)
	   (gov.nasa.worldwind.geom LatLon Position)
	   (gov.nasa.worldwind.render Polyline SurfacePolyline)
	   (gov.nasa.worldwind.avlist AVKey)
	   (gov.nasa.worldwind.awt WorldWindowGLCanvas)
	   (gov.nasa.worldwind Configuration WorldWind)))

(defn points [f]
  (let [data (zip/xml-zip (xml/parse f))
	trkpt (xml-> data :trk :trkseg :trkpt)]
    (map #(vector (attr % :lat) 
		  (attr % :lon) 
		  (first (xml-> % :ele text))) trkpt)))

(defn world []
  (Configuration/setValue AVKey/INITIAL_LATITUDE 39.3113)
  (Configuration/setValue AVKey/INITIAL_LONGITUDE 32.8038)
  (Configuration/setValue AVKey/INITIAL_ALTITUDE 1000000)
  (doto (WorldWindowGLCanvas.)
    (.setModel (WorldWind/createConfigurationComponent 
		AVKey/MODEL_CLASS_NAME))))

(defn surface-polyline [points]
  (let [list (ArrayList.)]
    (doseq [p points] 
      (.add list (LatLon/fromDegrees (Double. (p 0)) (Double. (p 1)))))
    (doto (RenderableLayer.)
      (.addRenderable (SurfacePolyline. list)))))

(defn polyline [points]
  (let [list (ArrayList.)]
    (doseq [p points] 
      (.add  list (Position. 
		   (LatLon/fromDegrees (Double. (p 0)) (Double. (p 1)))
		   (Double. (p 2)))))
    (doto (RenderableLayer.) 
      (.addRenderable (Polyline. list)))))

(defn frame []
  (let [world (world)
	layers (.getLayers (.getModel world))]
    (.add layers (surface-polyline (points "data.gpx")))
    ;;(.add layers (polyline (points "data.gpx")))
    (doto (javax.swing.JFrame.)
      (.add world)
      (.setSize (Dimension. 300 300))
      (.setAlwaysOnTop true)
      (.setVisible true))))

;;(frame)
