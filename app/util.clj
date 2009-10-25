(ns app.util
  (:import (java.io BufferedReader FileReader File InputStreamReader)
	   (java.text SimpleDateFormat)))

(def posts-per-page 2)

(defn convert-date [in-format out-format date]
  (.format (SimpleDateFormat. in-format)
	   (.parse (SimpleDateFormat. out-format) date)))

(defn read-file [file]
  (apply str
	 (interleave 
	  (line-seq 
	   (BufferedReader. (FileReader. file)))
	  (repeat \newline ))))

(defn file-to-url [file]
  (let [name (.replaceAll file ".markdown" "") ] 
    (str (apply str (interleave (repeat \/) (.split name "-" 4))) "/")))

(defn cmd [p] (.. Runtime getRuntime (exec (str p))))

(defn cmdout [o]
  (let [r (BufferedReader.
             (InputStreamReader.
               (.getInputStream o)))]
    (dorun (map println (line-seq r)))))
