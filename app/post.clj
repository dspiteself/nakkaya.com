(ns app.post
  (:use compojure)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.util)
  (:use :reload-all app.template)
  (:use :reload-all app.markdown))

(defn post-snippet [url title snippet]
  (html [:h2 [:a {:href url} title]] [:p snippet] [:br][:br]))

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
