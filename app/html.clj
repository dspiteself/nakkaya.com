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

(defn- archives-list
  "Create a list of months for posts available."
  []
  (let [months (post-count-by-mount)]
    (list [:h2 "Archives"]
	  [:ul (map 
		#(let [url (str "/" (.replace (first %) "-" "/") "/")
		       date (convert-date "MMMM yyyy" "yyyy-MM" (first %))
		       count (str " (" (second %) ")")]
		   [:li [:a {:href url} date] count]) months)])))

(defn archives
  "Create archive page."
  ([]
     (render-template 
      {:metadata {:title "Archives" :type 'tags :robots [:noindex :follow]} 
       :content (archives-list)}))
  ([year month]
     (let [time-raw (str year "-" month)
	   time-fs  (convert-date "MMMM yyyy" "yyyy-MM" time-raw)
	   meta {:title (str "Archives - " time-fs) 
		 :type 'archives :robots [:noindex :follow]}
	   content (map 
		    render-snippet
		    (filter #(.startsWith % time-raw) (post-list-by-date)))]
       (render-template {:metadata meta  :content content}))))

(defn post
  "Render and return the post if it exists."
  [year month day title]
  (let [file (File.
	      (str "posts/" year "-" month "-" day "-" title".markdown"))]
    (if (.exists file)
      (let [{meta :metadata content :content}  (markdown file)]
	(render-template
	 {:metadata (conj meta {:type 'post :file-name (.getName file)})
	  :content content})))))

(defn site
  "Return both pages and public files."
  [file]
  (let [site-path (File. (str "site/" file))
	public-path (File. (str "public/" file))]
    (cond (.exists site-path)
	  (let [{meta :metadata content :content} (markdown site-path)]
	    (render-template {:metadata (conj meta {:type 'page})
			      :content (list [:h2 (:title meta)] content)}))
	  (and (.exists public-path)
	       (= (.isDirectory public-path) false)) public-path)))

(defn file-not-found []
  (render-not-found))
