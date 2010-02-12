(ns app.template
  (:use compojure)
  (:use :reload-all app.util)
  (:use :reload-all app.storage)
  (:use :reload-all app.widgets))

(defn render-template [page]
  (let [meta (:metadata page)
	content  (:content page)]
    (html
     (doctype :xhtml-transitional)
     [:html
      {:xmlns "http://www.w3.org/1999/xhtml", :lang "en", :xml:lang "en"}
      [:head
       [:meta
	{:http-equiv "content-type", :content "text/html; charset=UTF-8"}]
       [:meta {:name "description", :content (:description meta)}]
       [:meta {:name "keywords", :content (:tags meta)}]
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
       [:title (:title meta)]]
      [:body
       (analytics-js)
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
	  [:a {:href "/", :class "page"} "Home"] " | "
	  [:a {:href "/projects.markdown", :class "page"} "Projects"] " | "
	  [:a {:href "/contact.markdown", :class "page"} "About"]

	  [:form {:method "get" 
		  :action "http://www.google.com/search" :id "searchform"}
	   [:div
	    [:input {:type "text" :name "q" :class "box" :id "s"}]
	    [:input {:type "hidden" :name "sitesearch"
		     :value "nakkaya.com"}]]]]]
	[:div
	 {:id "content"}
	 [:div
	  {:id "post"}
	  (if (= (:type meta) 'post) [:h2 (:title meta)])

	   content

	  (if (= (:type meta) 'post)
	    (reduce 
	     (fn[h v]
	       (conj h [:a {:href (str "/tags/#" v)} (str v " ")]))
	     [:div {:class "post-tags"} "Tags: "] 
	     (.split (:tags meta) " ")))]

	 (if (= (:type meta) 'post)
	   [:div
	    {:id "related"}
	    [:h3 "Related Posts"]
	    [:ul
	     {:class "posts"}
	     (reverse
	      (reduce  
	       (fn[h v]
		 (conj 
		  h [:li 
		     [:span (:date v)] [:a {:href (:url v)} (:title v)]]))
	       () (similar-posts (:file-name meta) 3)))]])

	 [:div {:id "disqus"} 
	  (if (= (:type meta) 'post) (disqus-widget))]]
	[:div
	 {:id "footer"}
	 "Powered By"
	 [:a {:href "http://compojure.org/"} " Compojure"]
	 [:p "&copy; 2010" 
	  [:a {:href "/contact.markdown"} " Nurullah Akkaya"]]]]
       (if (= (:type meta) 'post) (disqus-js))]])))
