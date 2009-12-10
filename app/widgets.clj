(ns app.widgets
  (:use clojure.set)
  (:use :reload-all app.util)
  (:use :reload-all app.markdown)
  (:use :reload-all app.storage))

(defn tags-widget []
  (let [tags (reverse (sort (post-count-by-tags)))]
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

(defn- similarity [post1 post2]
  (let [shared-items (into #{} (filter #(some #{%} post1) post2))
	unique-item (difference (union (into #{} post1) (into #{} post2))
				shared-items)]
    (if (or (= (count shared-items) 0)
	    (= (count unique-item) 0))
      0
      (/ 1 (+ 1 (double (/ (count shared-items) 
			   (count unique-item))))))))

(defn- sort-by-similarity [posts post]
  (sort-by 
   second
   (reduce (fn[h p]
	     (let [url (first p)
		   tags (second p)
		   similarity (similarity post tags)]
	       (assoc h url similarity) )) {} posts)))

(defn similar-posts [file-name count]
  (let [posts (post-tag-map)
	sim-posts 
	(take count (reverse (sort-by-similarity posts (posts file-name))))]
    (reduce (fn[h p]
	      (let [file (first p)
		    url  (file-to-url file)
		    metadata (:metadata (read-markdown (str "posts/" file)))
		    title (metadata "title")
		    date (file-to-date file)]
		(conj h {:url url :title title :date date}))) [] sim-posts)))


(defn disqus-widget []
  "<div id=\"disqus_thread\"></div><script type=\"text/javascript\" src=\"http://disqus.com/forums/nakkaya/embed.js\"></script><noscript><a href=\"http://disqus.com/forums/nakkaya/?url=ref\">View the discussion thread.</a></noscript><a href=\"http://disqus.com\" class=\"dsq-brlink\">blog comments powered by <span class=\"logo-disqus\">Disqus</span></a>")

(defn disqus-js []
   "<script type=\"text/javascript\">
//<![CDATA[
(function() {
	var links = document.getElementsByTagName('a');
	var query = '?';
	for(var i = 0; i < links.length; i++) {
	if(links[i].href.indexOf('#disqus_thread') >= 0) {
		query += 'url' + i + '=' + encodeURIComponent(links[i].href) + '&';
	}
	}
	document.write('<script charset=\"utf-8\" type=\"text/javascript\" src=\"http://disqus.com/forums/nakkaya/get_num_replies.js' + query + '\"></' + 'script>');
})();
//]]>
</script>")

(defn analytics-js []
  "<script type=\"text/javascript\">
var gaJsHost = ((\"https:\" == document.location.protocol) ? \"https://ssl.\" : \"http://www.\");
document.write(unescape(\"%3Cscript src='\" + gaJsHost + \"google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E\"));
</script>
<script type=\"text/javascript\">
try {
var pageTracker = _gat._getTracker(\"UA-87333-8\");
pageTracker._trackPageview();
} catch(err) {}</script>")
