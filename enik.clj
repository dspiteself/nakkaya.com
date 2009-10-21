(ns enik
  (:use :reload-all compojure)
  (:use :reload-all app.util)
  (:use :reload-all [app.template :only [render-template render-page]])
  (:use :reload-all [app.html :only [rss tags latest-posts archives]])
  (:import (java.io File)
	   (java.text SimpleDateFormat)))

(defn serve-post [year month day title]
  (let [file (str "posts/" year "-" month "-" day "-" title".markdown")]
    (if (.exists (File. file))
      (render-page file)) ))

(defn serve-lastest-posts [page]
  (render-template 
   {:metadata {"title" site-title 
	       "layout" "default"
	       "tags" "nurullah akkaya"
	       "description" "Nurullah Akkaya's Latest Posts" }
    :content (latest-posts page)}))

(defn serve-tags-page []
  (render-template 
   {:metadata {"title" "Tags" "layout" "default"} :content  (tags)}))

(defn serve-archives [year month]  
  (let [time (.format 
	      (SimpleDateFormat. "MMMM yyyy")
	      (.parse (SimpleDateFormat. "yyyy-MM") (str year "-" month)))]
  (render-template 
   {:metadata {"title" (str "Archives - " time) "layout" "default"}
    :content  (archives (str year "-" month) ) })))

(defn serve-site [file]
  (let [full-path (str "site/" file)]
    (if (.exists (File. full-path))
      (cond
       (.endsWith full-path "/") (serve-lastest-posts 0)
       (.endsWith full-path ".markdown") (render-page  full-path)))))

(defn cache-markdown []
  (def mem-serve-site (memoize serve-site))
  (def mem-serve-post (memoize serve-post))
  (def mem-serve-latest-posts (memoize serve-lastest-posts))
  (def mem-serve-tags-page (memoize serve-tags-page))
  (def mem-serve-archives (memoize serve-archives))
  (def mem-serve-rss (memoize rss)))

(cache-markdown)

(defn github-hook []
  (println "Pulling Changes...")
  (try (cmd "git pull") (catch Exception e))
  (cache-markdown))

(defroutes enik
  (POST "/github-hook"
       (or (github-hook) :next))
  (GET "/tags/"
       (or (mem-serve-tags-page) :next))
  (GET "/latest-posts/:page/"
       (or (mem-serve-latest-posts (:page params)) :next))
  (GET "/:year/:month/"
       (or (mem-serve-archives (:year params) (:month params)) :next))
  ;;blog related routes
  (GET "/:year/:month/:day/:title/"
       (or (mem-serve-post (:year params)
			   (:month params)
			   (:day params) 
			   (:title params)) :next))
  (GET "/rss-feed"
       (or [(content-type "text/xml")
	    (mem-serve-rss)] :next))
  ;;site related routes
  (GET "/*" 
       (or (mem-serve-site (params :*)) :next))
  (GET "/*" 
       (or (serve-file (params :*)) :next))
  (ANY "*"
       [(content-type "text/html") (page-not-found)]))

(run-server {:port 8085} "/*" (servlet enik))
