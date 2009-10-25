(ns app.widgets
  (:use :reload-all app.util)
  (:use :reload-all app.storage))

(defn tags-widget []
  (let [tags (post-count-by-tags)] 
     [:div {:class "widget"} 
      [:h5 "Tags"]
      [:ul
       (reduce 
	(fn [h v]
	  (let [tag (first v)
		count (str " (" (second v) ")")] 
	    (conj h [:li [:a {:href (str "/tags/#" tag)} tag] count]))) 
	() tags)]]))

(defn archives-widget []
  (let [months (post-count-by-mount)]
     [:div {:class "widget"} 
      [:h5 "Archives"]
      [:ul
       (reduce 
	(fn [h v]
	  (let [url (str "/" (.replace (first v) "-" "/") "/")
		date (convert-date "MMMM yyyy" "yyyy-MM" (first v))
		count (str " (" (second v) ")")]
	    (conj h [:li [:a {:href url} date] count])))
	() months)]]))

(defn disqus-widget []
   [:div {:id "disqus_thread"}]
   [:script
    {:type "text/javascript",
     :src "http://disqus.com/forums/nakkaya/embed.js"}]
   [:noscript
    [:a
     {:href "http://disqus.com/forums/nakkaya/?url=ref"}
     "View the discussion thread."]]
   [:a
    {:href "http://disqus.com", :class "dsq-brlink"}
    "blog comments powered by"
    [:span {:class "logo-disqus"} "Disqus"]])

(defn disqus-js []
   [:script
    {:type "text/javascript"}
    "//\n(function() {\n\tvar links = document.getElementsByTagName('a');\n\tvar query = '?';\n\tfor(var i = 0; i < links.length; i++) {\n\tif(links[i].href.indexOf('#disqus_thread') >= 0) {\n\t\tquery += 'url' + i + '=' + encodeURIComponent(links[i].href) + '&';\n\t}\n\t}\n\tdocument.write('<script charset="
    'utf-8
    " type="
    'text/javascript
    " src="
    'http://disqus.com/forums/nakkaya/get_num_replies.js
    '+
    'query
    +
    '"></' + 'script>');\n})();\n//"])
