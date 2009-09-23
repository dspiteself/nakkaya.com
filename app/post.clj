(ns app.post
  (:use compojure)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.config)
  (:use :reload-all [app.util :only [post-list-by-date file-to-url]])
  (:use :reload-all [app.template :only [render-template]])
  (:use :reload-all [app.markdown :only [read-markdown]]))

(defn post-snippet [url title snippet]
  (html [:h2 [:a {:href url} title]] [:p snippet] [:br][:br]))

(defn render-snippet [file]
  (let [post (read-markdown (str "posts/" file))
	metadata (:metadata post)
	content  (:content post)]
    (post-snippet (file-to-url file) (metadata "title") content) ))

(defn paging [begin end content]
  (let [content (StringBuilder. content)
	page (/ begin posts-per-page)]

    (if (< end (count (post-list-by-date)))
      (.append 
       content 
       (html 
	[:div {:class "alignleft"}
	 [:a {:href (str "/latest-posts/" (+ page 1) "/")} 
	  "&laquo; Older Entries"]])))

    (if (< 0 page)
      (.append 
       content 
       (html 
	[:div {:class "alignright"}
	 [:a {:href (str "/latest-posts/" (- page 1) "/")} 
	  "Newer Entries &raquo;"]])))

    (.toString content)))

(defn render-snippets [begin end]
  (loop [posts (drop  begin (take end (post-list-by-date)))
	 post  (first posts)
	 content (str)]
    (if (empty? posts)
      (paging begin end content)
      (recur (rest posts)
	     (first (rest posts))
	     (str content (render-snippet post))))))

(defn latest-posts [page]
  (let [begin (* (Integer. page) posts-per-page) 
	end   (+ begin posts-per-page)]
    (render-template 
     {:metadata {"title" "Latest Posts" "layout" "default"}
      :content (render-snippets begin end)})))
