(ns app.routes
  (:use :reload-all compojure)
  (:use :reload-all app.util)
  (:require [app.html :as html]))

(defn cached-pages []
  (def site (memoize html/site))
  (def post (memoize html/post))
  (def latest-posts (memoize html/latest-posts))
  (def tags (memoize html/tags))
  (def archives (memoize html/archives))
  (def rss (memoize html/rss)))

(defn pages []
  (def site html/site)
  (def post html/post)
  (def latest-posts html/latest-posts)
  (def tags html/tags)
  (def archives html/archives)
  (def rss html/rss))

(if (nil? (System/getProperty "compojure.cache"))
  (do 
    (cached-pages)
    (cached-markdown))
  (pages))

(defn github-hook []
  (println "Pulling Changes...")
  (try (cmd "git pull") (catch Exception e))
  (cached-pages)
  (cached-markdown)
  "OK")

(defn redirect-301 [loc]
  [301 {:headers {"Location" loc}}])

(defroutes web-app
  (POST "/github-hook"
       (or (github-hook) :next))
  (GET "/tags/"
       (or (tags) :next))
  (GET "/latest-posts/:page/"
       (or (latest-posts (:page params)) :next))
  (GET "/archives/"
       (or (archives) :next))
  (GET "/:year/:month/"
       (or (archives (:year params) (:month params)) :next))
  ;;blog related routes
  (GET "/:year/:month/:day/:title/"
       (or (time (post (:year params) (:month params) (:day params) 
		       (:title params))) :next))
  (GET "/rss-feed"
       (or [(content-type "text/xml")
	    (rss)] :next))
  ;;site related routes
  (GET "/"
       (or (latest-posts 0) :next))
  (GET "/*" 
       (or (site (params :*)) :next))
  ;;redirects
  (GET "/:year/:month/:day/:title"
       (redirect-301 (str "/" (:year params) "/" (:month params)"/" 
			  (:day params) "/" (:title params) "/")))
  (ANY "*"
       [404 (content-type "text/html") (html/file-not-found)]))
