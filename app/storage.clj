(ns app.storage
  (:use clojure.set)
  (:use clojure.contrib.str-utils)
  (:use :reload-all [app.util :only [file-to-url file-name-to-date]])
  (:use :reload-all [app.markdown :only [read-markdown]])
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
		(assoc months date (+ 1 count) )
		)) {} posts) ))

(defn tag-post [post]
  (let [metadata (:metadata (read-markdown (str "posts/" post)))
	url      (file-to-url post)] 
    (reduce (fn 
	      [h v] 
	      (conj h { :tag v :post {:url url :title (metadata "title")}}))
    	    #{}
    	    (.split (metadata "tags") " "))  ))

(defn post-count-by-tags []
  (let [tag-set      (apply union (map tag-post (post-list-by-date)))]
    (reduce
     (fn [h v]
       (let [tag (:tag v)
	     count (get h tag 0)] 
	 (assoc h tag (+ 1 count) ))) {} tag-set) ))

(defn tag-set []
  (apply union (map tag-post (post-list-by-date))))

(defn- post-tag-map []
  (reduce 
   (fn[h post]
     (let [tags ((:metadata (read-markdown (str "posts/" post))) "tags")]
       (assoc h post (re-split #" " tags))))
   {} (post-list-by-date)))

(defn- similarity [post1 post2]
  (let [shared-items (into #{} (filter #(some #{%} post1) post2))
	unique-item (difference (union (into #{} post1) (into #{} post2))
				shared-items)]
    (if (or (= (count shared-items) 0)
	    (= (count unique-item) 0))
      0
      (/ 1 (+ 1 (double (/ (count shared-items) 
			   (count unique-item))))))))

(defn- sort-by-similarity [posts post]
  (sort-by 
   second
   (reduce (fn[h p]
	     (let [url (first p)
		   tags (second p)
		   similarity (similarity post tags)]
	       (assoc h url similarity) )) {} posts)))

(defn similar-posts [file-name count]
  (let [posts (post-tag-map)
	sim-posts 
	(take count (reverse (sort-by-similarity posts (posts file-name))))]
    (reduce (fn[h p]
	      (let [file (first p)
		    url  (file-to-url file)
		    metadata (:metadata (read-markdown (str "posts/" file)))
		    title (metadata "title")
		    date (file-name-to-date file)]
		(conj h {:url url :title title :date date}))) [] sim-posts)))
