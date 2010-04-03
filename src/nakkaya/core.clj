(ns nakkaya.core
  (:use compojure.core)
  (:use ring.adapter.jetty)
  (:use ring.middleware.file)
  (:use :reload-all nakkaya.util)
  (:require [nakkaya.html :as html]))

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

(if (nil? (System/getProperty "compojure.no-cache"))
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

(defn redirect-301 [params]
  (let [{year :year month :month day :day title :title} params]
    {:status  301
     :headers {"Location" (str "/" year "/" month"/" day "/" title "/")}}))

(defroutes nakkaya-routes
  (POST "/github-hook" [] (or (github-hook) :next))
  (GET  "/tags/" [] (or (tags) :next))
  (GET "/latest-posts/:page/" {params :params} 
       (or (latest-posts params) :next))
  (GET "/archives/" [] (or (archives) :next))
  (GET "/:year/:month/" {params :params} (or (archives params) :next))
  ;;blog related routes
  (GET "/:year/:month/:day/:title/" {params :params} 
       (or (time (post params))))
  (GET "/rss-feed" [] 
       {:status 200 :headers {"Content-Type" "text/xml"} :body (rss)})
  ;;site related routes
  (GET "/" [] (or (latest-posts {:page 0}) :next))
  (GET "/*.markdown"  {params :params} (or (site params) :next))
  ;;redirects
  (GET "/:year/:month/:day/:title" {params :params} (redirect-301 params))
  (ANY "*" []
       {:status  404 :headers {"Content-Type" "text/html"}
	:body (html/file-not-found)}))

(def app (-> nakkaya-routes
	     (wrap-file "public")))

(if (System/getProperty "compojure.site")
  (run-jetty app {:port 8085}))
