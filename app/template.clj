(ns app.template
  (:use compojure)
  (:use :reload-all app.util)
  (:use :reload-all app.storage)
  (:use :reload-all app.widgets))

(def site-title "Nurullah Akkaya")
(def site-url   "http://nakkaya.com")
(def site-desc  "useless homepage for pointless projects.")

(defn render-template [page]
  (let [metadata (:metadata page)
	content  (:content page)]
    (html
     [:html
      {:xmlns "http://www.w3.org/1999/xhtml", :lang "en", :xml:lang "en"}
      [:head
       [:meta
	{:http-equiv "content-type", :content "text/html; charset=UTF-8"}]
       [:meta {:name "description", :content (metadata "description")}]
       [:meta {:name "keywords", :content (metadata "tags")}]
       [:meta {:name "author", :content "Nurullah Akkaya"}]
       [:link {:rel "icon", 
	       :href "/images/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "shortcut icon", 
	       :href "/images/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "stylesheet", :type "text/css", :href "/default.css"}]
       [:link
	{:rel "alternate", :type "application/rss+xml",
	 :title site-title, :href "/rss-feed"}]
       [:script {:src "/highlight.pack.js", :type "text/javascript"}]
       [:script {:type "text/javascript"} "hljs.initHighlightingOnLoad();"]
       [:title (metadata "title")]]
      [:body
       [:div
	{:id "wrap"}
	[:div
	 {:id "header"}
	 [:h1
	  [:a
	   {:href "/"}
	   "nakkaya"
	   [:span {:class "fade-small"} "dot"]
	   [:span {:class "fade"} "com"]]]
	 [:div
	  {:class "pages"}
	  [:a {:href "/mocha.markdown", :class "page"} "Mocha"] " | "
	  [:a {:href "/makeWay.markdown", :class "page"} "makeWay"] " | "
	  [:a {:href "/experiments.markdown", :class "page"} 
	   "eXperiments"] " | "
	  [:a {:href "/contact.markdown", :class "page"} "About"]]]
	[:div
	 {:id "content"}
	 [:div
	  {:id "post"}
	  (if (= (:type metadata) 'post) [:h2 (metadata "title")])

	   content

	  (if (= (:type metadata) 'post)
	    (reduce 
	     (fn[h v]
	       (conj h [:a {:href (str "/tags/#" v)} (str v " ")]))
	     [:div {:class "post-tags"} "Tags: "] 
	     (.split (metadata "tags") " ")))]

	 [:div {:id "disqus"} 
	  (if (= (:type metadata) 'post) (disqus-widget))]
	 ;; [:div
	 ;;  {:id "related"}
	 ;;  [:h3 "Possibly Related Posts"]
	 ;;  [:ul
	 ;;   {:class "posts"}
	 ;;   [:li [:span "04 Nov 2009"] [:a {:href "/"} "Title"]]
	 ;;   [:li [:span "30 Oct 2009"] [:a {:href "/"} "Title"]]]]
	 ]
	[:div
	 {:id "footer"}
	 "Powered By"
	 [:a {:href "http://compojure.org/"} " Compojure"]
	 [:p "&copy; 2009" 
	  [:a {:href "/contact.markdown"} " Nurullah Akkaya"]]]]
       (analytics-js)
       (if (= (:type metadata) 'post) (disqus-js))]])))
