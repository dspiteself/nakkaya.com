(ns app.storage
  (:use clojure.set)
  (:use clojure.contrib.str-utils)
  (:use clojure.contrib.seq-utils)
  (:use :reload-all app.util)
  (:import (java.io File)))


(defn post-list-by-date []
  (let [dir (new File "posts/")]
    (reverse
     (sort
      (loop [files (.list dir)
	     list  []]
	(if (empty? files)
	  list
	  (recur (rest files) (conj list (first files)))))))))

(defn post-count-by-mount []
  (let [posts (post-list-by-date)]
    (reduce (fn [months post]
	      (let  [v (.split post "-" 3)
		     date (str (first v) "-" (second v))
		     count (get months date 0)]
		(assoc months date (+ 1 count)))) {} posts)))

(defn tag-post [post]
  (let [meta (:metadata (read-markdown (str "posts/" post)))
	url  (file-to-url post)]
    (reduce (fn 
	      [h v] 
	      (conj h {:tag v :post {:url url :title (:title meta)}}))
    	    #{}
    	    (.split (:tags meta) " "))))

(defn post-count-by-tags []
  (let [tag-set (apply union (map tag-post (post-list-by-date)))]
    (reduce
     (fn [h v]
       (let [tag (:tag v)
	     count (get h tag 0)] 
	 (assoc h tag (+ 1 count) ))) {} tag-set)))

(defn tag-set []
  (apply union (map tag-post (post-list-by-date))))

(defn post-tag-map []
  (reduce 
   (fn[h post]
     (let [tags (:tags (:metadata (read-markdown (str "posts/" post))))]
       (assoc h post (re-split #" " tags))))
   {} (shuffle (post-list-by-date))))
