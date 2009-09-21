(ns app.post
  (:use compojure)
  (:use :reload-all app.util)
  (:use :reload-all app.template)
  (:use :reload-all app.markdown)
  (:import (java.io File)))

(defn post-snippet [url title snippet]
  (html [:h2 [:a {:href url} title]] [:p snippet]))

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

(defn render-snippet [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    (post-snippet (file-to-url file) (metadata "title") content) ))

(defn render-snippets []
  (loop [posts (post-list-by-date)
	 post (first posts)
	 content (str)]
    (if (empty? posts)
      content
      (recur (rest posts)
	     (first (rest posts))
	     (str content (render-snippet post))))))

(defn latest-posts []
  (render-template 
   {:metadata {"title" "Latest Posts" "layout" "default"}
    :content (render-snippets)}))
