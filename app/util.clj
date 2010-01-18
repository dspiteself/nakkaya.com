(ns app.util
  (:use :reload-all clojure.contrib.str-utils)
  (:import (java.io BufferedReader FileReader File InputStreamReader)
	   (com.petebevin.markdown MarkdownProcessor)
	   (java.text SimpleDateFormat)))

(def site-title "an explorer's log")
(def site-url   "http://nakkaya.com")
(def site-desc  "Random bits and pieces on stuff that I find interesting.")
(def archives-desc "an explorer's log - Archives - Page ")
(def posts-per-page 2)

(defn convert-date [in-format out-format date]
  (.format (SimpleDateFormat. in-format)
	   (.parse (SimpleDateFormat. out-format) date)))

(defn- read-file [file]
  (apply str
	 (interleave 
	  (line-seq 
	   (BufferedReader. (FileReader. file)))
	  (repeat \newline ))))

(def markdown-processor (MarkdownProcessor.))

(defn- render-markdown [txt]
  (.markdown markdown-processor txt))

(defn- split-file 
  "split file into metadata and content"
  [content]
  (let [split-index (.indexOf content "---" 4)
	metadata (.substring content 4 split-index)
	content  (.substring content   (+ 3 split-index))] 
    {:metadata metadata :content content} ))

(defn- prepare-metadata [metadata]
  (reduce (fn [h [_ k v]] 
	    (let [key (keyword k)]
	      (assoc h key v)))
	  {} (re-seq #"([^:]+): (.+)(\n|$)" metadata)))

(defn read-markdown [file]
  (let [content (read-file file)
	page (split-file content)
	metadata (prepare-metadata (:metadata page))
	html (render-markdown (:content page))]
    {:metadata metadata :content html} ))


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
