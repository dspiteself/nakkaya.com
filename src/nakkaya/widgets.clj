(ns nakkaya.widgets
  (:use clojure.set)
  (:use :reload-all nakkaya.util)
  (:use :reload-all nakkaya.storage))

(defn- similarity [post1 post2]
  (let [shared-items (into #{} (filter #(some #{%} post1) post2))
	unique-items (difference (union (into #{} post1) (into #{} post2))
				 shared-items)]
    (cond 
      (= (count unique-items) 0) 1
      (= (count shared-items) 0) 0
      :else (/ 1 (+ 1 (double (/ (count shared-items) 
				 (count unique-items))))))))

(defn- sort-by-similarity [posts post]
  (sort-by 
   second
   (reduce (fn[h p]
	     (let [url (first p)
		   tags (second p)
		   similarity (similarity post tags)]
	       (assoc h url similarity) )) {} posts)))

(defn similar-posts [file-name count]
  (let [p-map (post-tag-map)
	post  (p-map file-name)
	posts (dissoc p-map file-name)
	sim-posts 
	(take count (reverse (sort-by-similarity posts post)))]
    (reduce (fn[h p]
	      (let [file (first p)
		    url  (file-to-url file)
		    meta (:metadata (markdown (str "posts/" file)))
		    title (:title meta)
		    date (file-to-date file)]
		(conj h {:url url :title title :date date}))) [] sim-posts)))

;(doseq[post (post-list-by-date)] (println (similar-posts post 5)))
;(similar-posts "2009-12-11-type-less-to-type-more.markdown" 5)

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
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-87333-8']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(ga);
  })();

</script>")
