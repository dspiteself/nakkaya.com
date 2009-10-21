(ns app.html
  (:use compojure)
  (:use clojure.set)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.util)
  (:use :reload-all app.storage)
  (:use :reload-all [app.markdown :only [read-markdown]])
  (:import (java.text SimpleDateFormat)))


(defn- post-xml [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    [:item 
     [:title (metadata "title")]
     [:link  (str site-url (file-to-url file))]
     [:description content]]))

(defn- posts-feed []
  (loop [posts (post-list-by-date)
	 post  (first posts)
	 feed  ()]
    (if (empty? posts)
      feed
      (recur (rest posts)
	     (first (rest posts))
	     (conj feed (post-xml post))))))

(defn rss []
  (with-out-str
   (prxml [:decl! {:version "1.0"}] 
	  [:rss {:version "2.0"} 
	   [:channel 
	    [:title site-title]
	    [:link site-url]
	    [:description site-desc]
	    (take 10 (reverse (posts-feed)))]])))

(defn- tag-list [tag tag-set]
  (let [posts (project (select #(= (:tag %) tag) tag-set ) [:post])]
    (reduce 
     (fn [h v] 
       (conj h [:li [:a {:href (:url (:post v))} (:title (:post v))]]  ))
     [:ul ] posts) ))

(defn- tag-page-content [tag-set tag-distinct]
  (html
   (reduce
    (fn [h v]
      (conj h [:h4 [:a {:name (:tag v)} (:tag v)]] 
	    (tag-list (:tag v) tag-set)  ))
    [:div ] tag-distinct)))

(defn tags []
  (let [tag-set      (tag-set)
	tag-distinct (project tag-set [:tag])
	content      (tag-page-content tag-set tag-distinct)]
    (tag-page-content tag-set tag-distinct)))

(defn- file-name-to-date [file]
  (let  [parse-format (SimpleDateFormat. "yyyy-MM-dd")
	 date (.parse parse-format (re-find #"\d*-\d*-\d*" file)) 
	 print-format (SimpleDateFormat. "EEEE, dd - MMMM - yyyy")]
    (.format print-format date)))

(defn- post-snippet [url tags date title snippet]
  (html [:h2
	 [:a {:href url} title]] 
	[:h5 {:class "post-date"}  date]
	[:p snippet]  [:h4 [:a {:href "/tags/"} "Tags: "] tags] ))

(defn- render-snippet [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    (post-snippet (file-to-url file) 
		  (metadata "tags")
		  (file-name-to-date file)
		  (metadata "title") 
		  content) ))

(defn- paging [begin end content]
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

(defn- render-snippets [begin end]
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

(defn archives [time]
  (let [posts (filter #(.startsWith % time) (post-list-by-date))]
    (html 
     (reduce (fn[h v]
	       (conj h (render-snippet v))) [:div] posts)) ))
