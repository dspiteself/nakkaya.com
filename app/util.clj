(ns app.util
  (:import (java.io BufferedReader FileReader File InputStreamReader)
	   (java.text SimpleDateFormat)))

(def site-title "an explorer's log")
(def site-url   "http://nakkaya.com")
(def site-desc  "Random bits and pieces on stuff that I find interesting.")
(def archives-desc "an explorer's log - Archives - Page ")
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

(defn file-to-date [file]
  (let  [parse-format (SimpleDateFormat. "yyyy-MM-dd")
	 date (.parse parse-format (re-find #"\d*-\d*-\d*" file)) 
	 print-format (SimpleDateFormat. "dd MMM yyyy")]
    (.format print-format date)))

(defn cmd [p] (.. Runtime getRuntime (exec (str p))))

(defn cmdout [o]
  (let [r (BufferedReader.
             (InputStreamReader.
               (.getInputStream o)))]
    (dorun (map println (line-seq r)))))
