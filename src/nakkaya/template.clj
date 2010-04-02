(ns nakkaya.template
  (:use hiccup.core)
  (:use hiccup.page-helpers)
  (:use :reload-all nakkaya.util)
  (:use :reload-all nakkaya.storage)
  (:use :reload-all nakkaya.widgets))

(defn template [page]
  (let [meta (:metadata page)
	content  (:content page)]
    (doctype :xhtml-transitional)
    [:html
     {:xmlns "http://www.w3.org/1999/xhtml", :lang "en", :xml:lang "en"}
     [:head
      [:meta
       {:http-equiv "content-type", :content "text/html; charset=UTF-8"}]
      [:meta {:name "description", :content (:description meta)}]
      [:meta {:name "keywords", :content (:tags meta)}]
      [:meta {:name "author", :content "Nurullah Akkaya"}]

      (if-let [directives (:robots meta)]
	[:meta {:name "robots", 
		:content 
		(apply str (interpose "," (map name directives)))}])

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
      (if (= (:type meta) 'post) (disqus-js))]]))

(defn render-template [page]
  (html (template page)))

(defn render-not-found []
  (html
   [:html
    {:xml:lang "en", :xmlns "http://www.w3.org/1999/xhtml", :lang "en"}
    [:head
     [:meta
      {:http-equiv "Content-Type", :content "text/html; charset=UTF-8"}]
     [:title "Move Along"]
     [:style
      {:type "text/css"}
      "html, body {\n      height: 100%;\n      }\n\n      #center {\n      width: 400px;\n      height: 400px;\n      text-align: center;\n\n      position: absolute;\n      left: 50%;\n      top: 50%;\n      margin-left: -200px;\n      /* Half the width of the DIV tag which is 50 pixels */\n      margin-top: -200px;\n      /* Half the height of the DIV tag which is also 50 pixels */\n      }"]]
    [:body
     [:div
      {:id "center"}
      [:p
       [:font
	{:face "Arial", :size "13"}
	"This is not the page you are looking for."
	[:br]
	[:a {:href "/"} "Move Along"]]]]]]))
