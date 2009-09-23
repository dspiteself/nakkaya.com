(ns enik
  (:use :reload-all compojure)
  (:use :reload-all [app.util :only [read-file cmd cmdout]])
  (:use :reload-all [app.rss :only [update-rss rss-feed]])
  (:use :reload-all [app.markdown :only [render-page]])
  (:use :reload-all [app.post :only [latest-posts]]))

(defn serve-site [file]
  (let [full-path (str "site/" file)]
    (cond
     (.endsWith full-path "/") (render-page 
				(str full-path "index.markdown"))
     (.endsWith full-path ".markdown") (render-page  full-path)) ))

(defn serve-post [year month day title]
  (let [file (str "posts/" year "-" month "-" day "-" title".markdown")]
    (render-page file)))

(defn github-hook []
  (println "Pulling Changes...")
  (cmdout (cmd "git pull"))
  (dosync (ref-set rss-feed (update-rss))))

(defroutes enik
  (POST "/github-hook"
       (or (github-hook) :next))
  ;;blog related routes
  (GET "/:year/:month/:day/:title/"
       (or (serve-post (:year params)
			    (:month params)
			    (:day params) 
			    (:title params)) :next))
  (GET "/latest-posts/:page/"
       (or (latest-posts (:page params)) :next))
  (GET "/rss-feed"
       (or [(content-type "text/xml")
	    @rss-feed] :next))
  ;;site related routes
  (GET "/*" 
       (or (serve-site (params :*)) :next))
  (GET "/*" 
       (or (serve-file (params :*)) :next))
  ;;layout related routes
  (GET "/*.css"        
       (or [(content-type "text/css")
	    (read-file (str "layouts/" (params :*) ".css"))] :next))
  (ANY "*"
       (page-not-found)))

(run-server {:port 8085} "/*" (servlet enik))
