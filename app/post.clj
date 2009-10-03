(ns app.post
  (:use compojure)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.config)
  (:use :reload-all [app.util :only [post-list-by-date file-to-url]])
  (:use :reload-all [app.markdown :only [read-markdown]])
  (:import (java.text SimpleDateFormat)))

(defn file-name-to-date [file]
  (let  [parse-format (SimpleDateFormat. "yyyy-MM-dd")
	 date (.parse parse-format (re-find #"\d*-\d*-\d*" file)) 
	 print-format (SimpleDateFormat. "EEEE, dd - MMMM - yyyy")]
    (.format print-format date)))

(defn post-snippet [url tags date title snippet]
  (html [:h2
	 [:a {:href url} title]] 
	[:h5 {:class "post-date"}  date]
	[:p snippet]  [:h4 [:a {:href "/tags/"} "Tags: "] tags] ))

(defn render-snippet [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    (post-snippet (file-to-url file) 
		  (metadata "tags")
		  (file-name-to-date file)
		  (metadata "title") 
		  content) ))

(defn paging [begin end content]
  (let [content (StringBuilder. content)
	page (/ begin posts-per-page)]

    (if (< end (count (post-list-by-date)))
      (.append 
       content 
       (html 
	[:div {:class "alignleft"}
	 [:a {:href (str "/latest-posts/" (+ page 1) "/")} 
	  "&laquo; Older Entries"]])))

    (if (< 0 page)
      (.append 
       content 
       (html 
	[:div {:class "alignright"}
	 [:a {:href (str "/latest-posts/" (- page 1) "/")} 
	  "Newer Entries &raquo;"]])))

    (.toString content)))

(defn render-snippets [begin end]
  (loop [posts (drop  begin (take end (post-list-by-date)))
	 post  (first posts)
	 content (str)]
    (if (empty? posts)
      (paging begin end content)
      (recur (rest posts)
	     (first (rest posts))
	     (str content (render-snippet post))))))

(defn latest-posts [page]
  (let [begin (* (Integer. page) posts-per-page) 
	end   (+ begin posts-per-page)]
    (render-snippets begin end)))


(defn post-count-by-mount []
  (let [posts (post-list-by-date)]
    (reduce (fn [months post]
	      (let  [v (.split post "-" 3)
		     date (str (first v) "-" (second v))
		     count (get months date 0)]
		(assoc months date (+ 1 count) )
		)) {} posts) ))

(defn posts-by-month [time]
  (let [posts (filter #(.startsWith % time) (post-list-by-date))]
    (html 
     (reduce (fn[h v]
	       (conj h (render-snippet v))) [:div] posts)) ))
