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
      {:xmlns "http://www.w3.org/1999/xhtml"}
      [:head
       {:profile "http://gmpg.org/xfn/11"}
       [:link {:rel "icon", :href "/images/favicon.ico" :type "image/x-icon"}]
       [:link {:rel "shortcut icon", :href "/images//favicon.ico" :type "image/x-icon"}]
       [:link
	{:rel "stylesheet", :type "text/css", :href "/default.css"}]
       [:link
	{:rel "alternate",
	 :type "application/rss+xml",
	 :title site-title,
	 :href "/rss-feed"}]
       [:meta
	{:http-equiv "Content-Type", :content "text/html; charset=UTF-8"}]
       [:meta {:name "keywords", :content (metadata "tags")}]
       [:meta {:name "description", :content (metadata "description")}]
       [:title (metadata "title")]
       [:script {:type "text/javascript", :src "/highlight.pack.js"}]
       [:script {:type "text/javascript"} "hljs.initHighlightingOnLoad();"]]
      [:body
       [:div
	{:id "wrap"}
	[:div
	 {:id "container"}
	 [:div
	  {:id "header"}
	  [:div
	   {:id "caption"}
	   [:h1
	    {:id "title"}
	    [:a {:href "http://nakkaya.com/"} "Nurullah Akkaya's Home"]]
	   [:div
	    {:id "tagline"}
	    "useless homepage for hosting pointless projects."]]
	  [:div
	   {:id "navigation"}
	   [:ul
	    {:id "menus"}
	    [:li
	     {:class "current_page_item"}
	     [:a {:class "home", :title "Home", :href "/"} "Home"]]
	    [:li
	     {:class "page_item page-item-2"}
	     [:a {:href "/mocha.markdown", :title "Mocha"} "Mocha"]]
	    [:li
	     {:class "page_item page-item-2"}
	     [:a {:href "/makeWay.markdown", :title "makeWay"} "makeWay"]]
	    [:li
	     {:class "page_item page-item-2"}
	     [:a
	      {:href "/experiments.markdown", :title "Experiments"}
	      "Experiments"]]
	    [:li
	     {:class "page_item page-item-2"}
	     [:a {:href "/contact.markdown", :title "About"} "About"]]
	    [:li [:a {:class "lastmenu", :href "javascript:void(0);"}]]]
	   [:div
	    {:id "searchbox"}
	    [:form
	     {:action "http://www.google.com/cse", :id "cse-search-box"}
	     [:div
	      {:class "content"}
	      [:input
	       {:type "hidden",
		:name "cx",
		:value "011327289396503571951:gmikuqrq7lm"}]
	      [:input {:type "hidden", :name "ie", :value "UTF-8"}]
	      [:input
	       {:class "textfield",
		:name "q",
		:size "24",
		:type "text",
		:value ""}]
	      [:input {:class "button", :type "submit", :value ""}]]]]
	   [:div {:class "fixed"}]]
	  [:div {:class "fixed"}]]
	 [:div
	  {:id "content"}
	  [:div
	   {:id "main"}
	   [:div
	    {:class "post", :id "post-1"}
	    [:div
	     {:class "content"}
	     content
	     [:p {:class "under"} 
	      (if (= (:type metadata) 'post) (disqus-widget))]
	     [:div {:class "fixed"}]]]]
	  [:div
	   {:id "sidebar"}
	   [:div
	    {:id "northsidebar", :class "sidebar"}
	    [:div
	     {:class "widget widget_feeds"}
	     [:div
	      {:class "content"}
	      [:div
	       {:id "subscribe"}
	       [:a
		{:rel "external nofollow",
		 :id "feedrss",
		 :title "Subscribe to this blog...",
		 :href "http://nakkaya.com/rss-feed"}
		[:abbr {:title "Really Simple Syndication"} "RSS"]]]
	      [:div {:class "fixed"}]]]
	    (archives-widget)
	    (tags-widget)
	    ]]
	  [:div {:class "fixed"}]]
	 [:div
	  {:id "footer"}
	  [:a
	   {:id "gotop", :href "#", :onclick "MGJS.goTop();return false;"}
	   "Top"]
	  [:div {:id "copyright"} "Nurullah Akkaya"]
	  [:div
	   {:id "themeinfo"}
	   "Powered By "
	   [:a {:href "http://github.com/weavejester/compojure"} "compojure"]
	   ", \n\t    Theme by"
	   [:a {:href "http://wordpress.org/extend/themes/inove"} "mg12"]
	   "."]]]]
       (analytics-js)
       (if (= (:type metadata) 'post) (disqus-js))]])))
