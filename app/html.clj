(ns app.html
  (:use compojure)
  (:use clojure.set)
  (:use :reload-all clojure.contrib.prxml)
  (:use :reload-all app.util)
  (:use :reload-all app.storage)
  (:use :reload-all app.template)
  (:import (java.text SimpleDateFormat)
	   (java.io File)))


(defn- post-xml
  "Create RSS item node."
  [file]
  (let [post (markdown (str "posts/" file))
	meta (:metadata post)
	content  (:content post)]
    [:item 
     [:title (:title meta)]
     [:link  (str site-url (file-to-url file))]
     [:description content]]))

(defn rss 
  "Create RSS feed."
  []
  (with-out-str
   (prxml [:decl! {:version "1.0"}] 
	  [:rss {:version "2.0"} 
	   [:channel 
	    [:title site-title]
	    [:link site-url]
	    [:description site-desc]
	    (take 10 (map post-xml (post-list-by-date)))]])))

(defn- tag-list 
  "Build a list of URLs for each post containing the tag."
  [tag tag-set]
  (let [posts (project (select #(= (:tag %) tag) tag-set) [:post])]
    [:ul (map #(vector :li [:a {:href (:url (:post %))} (:title (:post %))])
	      posts)]))

(defn- tag-page-content
  "Create content for tags page. For each unique tag create a list of posts."
  [tag-set tag-distinct]
  (map #(vector :h4 [:a {:name (:tag %)} (:tag %)]
		(tag-list (:tag %) tag-set)) tag-distinct))

(defn tags
  "Create tags page."
  []
  (let [tag-set (tag-set)
	tag-distinct (sort-by :tag (project tag-set [:tag]))
	meta  {:title "Tags" :type 'tags :robots [:noindex :follow]}
	content (html (tag-page-content tag-set tag-distinct))]
    (render-template {:metadata meta :content content})))

(defn- render-snippet
  "Render a post for display in index pages."
  [file]
  (let [post (markdown (str "posts/" file))
	meta (:metadata post)
	content  (:content post)]
    [:div [:h2 [:a {:href (file-to-url file)} (:title meta)]]
     [:p {:class "publish_date"}  (file-to-date file)]
     [:p content]]))

(defn- pager
  "Return bottom pager links for index pages."
  [begin end]
  (let [page (/ begin posts-per-page)
	older [:div {:class "pager-left"}
	       [:a {:href (str "/latest-posts/" (+ page 1) "/")} 
		"&laquo; Older Entries"]]
	archive [:div {:class "pager-right"} 
		 [:a {:href (str "/archives/")} "Archives"]]
	newer [:div {:class "pager-right"}
	       [:a {:href (str "/latest-posts/" (- page 1) "/")} 
		"Newer Entries &raquo;"]]]
    (cond (= page 0) (list older archive)
	  (and (< 0 page)
	       (< end (count (post-list-by-date)))) (list older newer)
	       :else (list newer))))

(defn- render-snippets
  "Build snippet list and pagers for navigation."
  [begin end]
  (let [posts (drop begin (take end (post-list-by-date)))]
     (list (map render-snippet posts) (pager begin end))))

(defn latest-posts
  "Create index pages."
  [page]
  (let [begin (* (Integer. page) posts-per-page) 
	end   (+ begin posts-per-page)
	title (if (= page 0) site-title (str archives-title page))
	m {:title title :tags "nurullah akkaya"
	   :description site-desc :type 'latest}
	meta (if (= page 0) m (assoc m :robots [:noindex :follow]))
	content (render-snippets begin end)]
    (render-template {:metadata meta  :content content})))

(defn- archives-list []
  (let [months (post-count-by-mount)]
    (html
     [:h2 "Archives"]
     [:ul
      (reduce 
       (fn [h v]
	 (let [url (str "/" (.replace (first v) "-" "/") "/")
	       date (convert-date "MMMM yyyy" "yyyy-MM" (first v))
	       count (str " (" (second v) ")")]
	   (conj h [:li [:a {:href url} date] count])))
       () months)])))

(defn archives 
  ([]
     (let [meta {:title "Archives" :type 'tags :robots [:noindex :follow]}
	   content (archives-list)]
       (render-template {:metadata meta :content content})))
  ([year month]
     (let [time  (convert-date "MMMM yyyy" "yyyy-MM" (str year "-" month))
	   meta {:title (str "Archives - " time) 
		 :type 'archives :robots [:noindex :follow]}
	   posts (filter 
		  #(.startsWith % (str year "-" month)) (post-list-by-date))
	   content (html
		    (reduce (fn[h v]
			      (conj h (render-snippet v))) [:div] posts))]
       (render-template {:metadata meta  :content content}))))

(defn post [year month day title]
  (let [file-name (str year "-" month "-" day "-" title".markdown")
	file (str "posts/" file-name)]
    (if (.exists (File. file))
      (let [page  (markdown file)
	    meta (conj (:metadata page) {:type 'post :file-name file-name})
	    content  (:content page)]
	(render-template {:metadata meta :content content})))))

(defn site [file]
  (let [site-path (str "site/" file)
	public-path (File. (str "public/" file))]
    (cond (.exists (File. site-path)) 
	  (let [page  (markdown site-path)
		meta (conj (:metadata page) {:type 'page})
		title    (:title meta)
		content  (str (html [:h2 title]) (:content page))]
	    (render-template {:metadata meta :content content}))
	  (and (.exists public-path)
	       (= (.isDirectory public-path) false)) public-path)))

(defn file-not-found []
  (render-not-found))
