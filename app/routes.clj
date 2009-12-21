(ns app.routes
  (:use :reload-all compojure)
  (:use :reload-all app.util)
  (:use :reload-all app.html))

(defn cache-markdown []
  (def mem-site (memoize site))
  (def mem-post (memoize post))
  (def mem-latest-posts (memoize latest-posts))
  (def mem-tags (memoize tags))
  (def mem-archives (memoize archives))
  (def mem-rss (memoize rss)))

(cache-markdown)

(defn github-hook []
  (println "Pulling Changes...")
  (try (cmd "git pull") (catch Exception e))
  (cache-markdown)
  "OK")

(defn redirect-301 [loc]
  [301 {:headers {"Location" loc}}])

(defroutes enik
  (POST "/github-hook"
       (or (github-hook) :next))
  (GET "/tags/"
       (or (mem-tags) :next))
  (GET "/latest-posts/:page/"
       (or (mem-latest-posts (:page params)) :next))
  (GET "/archives/"
       (or (mem-archives) :next))
  (GET "/:year/:month/"
       (or (mem-archives (:year params) (:month params)) :next))
  ;;blog related routes
  (GET "/:year/:month/:day/:title/"
       (or (mem-post (:year params) (:month params) (:day params) 
		     (:title params)) :next))
  (GET "/rss-feed"
       (or [(content-type "text/xml")
	    (mem-rss)] :next))
  ;;site related routes
  (GET "/"
       (or (mem-latest-posts 0) :next))
  (GET "/*" 
       (or (mem-site (params :*)) :next))
  ;;redirects
  (GET "/:year/:month/:day/:title"
       (redirect-301 (str "/" (:year params) "/" (:month params)"/" 
			  (:day params) "/" (:title params) "/")))
  (ANY "*"
       [404 (content-type "text/html") (file-not-found)]))

(run-server {:port 8085} "/*" (servlet enik))
