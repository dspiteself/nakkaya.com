(ns app.util
  (:import (java.io BufferedReader FileReader)))


(defn read-file [file]
  (apply str
	 (interleave 
	  (line-seq 
	   (BufferedReader. (FileReader. file)))
	  (repeat \newline ))))
