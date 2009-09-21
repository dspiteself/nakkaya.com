(ns app.util
  (:import (java.io BufferedReader FileReader File)))


(defn read-file [file]
  (apply str
	 (interleave 
	  (line-seq 
	   (BufferedReader. (FileReader. file)))
	  (repeat \newline ))))

(defn post-list-by-date []
  (let [dir (new File "posts/")]
    (reverse
     (sort
      (loop [files (.list dir)
	     list  []]
	(if (empty? files)
	  list
	  (recur (rest files) (conj list (first files)))))))))

(defn file-to-url [file]
  (let [name (.replaceAll file ".markdown" "") ] 
    (str (apply str (interleave (repeat \/) (.split name "-" 4))) "/")))
