(ns app.rss
  (:use compojure)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.config)
  (:use :reload-all app.util)
  (:use :reload-all app.markdown)
  (:use :reload-all app.template))

(def rss-feed (ref ""))

(defn post-xml [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    [:item 
     [:title (metadata "title")]
     [:link  (str site-url (file-to-url file))]
     [:description content]]))

(defn posts-feed []
  (loop [posts (post-list-by-date)
	 post  (first posts)
	 feed  ()]
    (if (empty? posts)
      feed
      (recur (rest posts)
	     (first (rest posts))
	     (conj feed (post-xml post))))))

(defn update-rss []
  (with-out-str
   (prxml [:decl! {:version "1.0"}] 
	  [:rss {:version "2.0"} 
	   [:channel 
	    [:title site-title]
	    [:link site-url]
	    [:description site-desc]
	    (posts-feed)]])))

(dosync (ref-set rss-feed (update-rss)))
