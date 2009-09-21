(ns enik
  (:use :reload-all compojure)
  (:use :reload-all app.util)
  (:use :reload-all app.rss)
  (:use :reload-all app.markdown)
  (:use :reload-all app.post))

(defn enik-serve-file [file]
  (let [full-path (str "site/" file)]
    (cond
     (.endsWith full-path "/") (render-page 
				(str full-path "index.markdown"))
     (.endsWith full-path ".markdown") (render-page  full-path)) ))

(defn enik-serve-post [year month day title]
  (let [file (str "posts/" year "-" month "-" day "-" title".markdown")]
    (render-page file)))

(defroutes enik
  ;;blog related routes
  (GET "/:year/:month/:day/:title/"
       (or (enik-serve-post (:year params)
			    (:month params)
			    (:day params) 
			    (:title params)) :next))
  (GET "/latest-posts"
       (or (latest-posts) :next))
  (GET "/rss-feed"
       (or [(content-type "text/xml")
	    @rss-feed] :next))
  ;;site related routes
  (GET "/*" 
       (or (enik-serve-file (params :*)) :next))
  (GET "/*" 
       (or (serve-file (params :*)) :next))
  ;;layout related routes
  (GET "/*.css"        
       (or [(content-type "text/css")
	    (read-file (str "layouts/" (params :*) ".css"))] :next))
  (ANY "*"
       (page-not-found)))

(run-server {:port 9993} "/*" (servlet enik))
