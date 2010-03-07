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

(defn- tag-list [tag tag-set]
  (let [posts (project (select #(= (:tag %) tag) tag-set ) [:post])]
    (reduce 
     (fn [h v] 
       (conj h [:li [:a {:href (:url (:post v))} (:title (:post v))]]))
     [:ul ] posts)))

(defn- tag-page-content [tag-set tag-distinct]
  (html
   (reduce
    (fn [h v]
      (conj h [:h4 [:a {:name (:tag v)} (:tag v)]]
	    (tag-list (:tag v) tag-set)))
    [:div ] tag-distinct)))

(defn tags []
  (let [tag-set      (tag-set)
	tag-distinct (sort-by :tag (project tag-set [:tag]))
	meta     {:title "Tags" :type 'tags :robots [:noindex :follow]}
	content      (tag-page-content tag-set tag-distinct)]
    (render-template {:metadata meta  :content content  })))

(defn- post-snippet [url date title snippet]
  (html [:h2 [:a {:href url} title]]
	[:p {:class "publish_date"}  date]
	[:p snippet] ))

(defn- render-snippet [file]
  (let [post (markdown (str "posts/" file))
	meta (:metadata post)
	content  (:content post)]
    (post-snippet 
     (file-to-url file) (file-to-date file) (:title meta) content)))

(defn- paging [begin end content]
  (let [content (StringBuilder. content)
	page (/ begin posts-per-page)]

    (if (< end (count (post-list-by-date)))
      (.append 
       content 
       (html 
	[:div {:class "pager-left"}
	 [:a {:href (str "/latest-posts/" (+ page 1) "/")} 
	  "&laquo; Older Entries"]])))

    (if (= 0 page)
      (.append 
       content 
       (html 
	[:div {:class "pager-right"} 
	 [:a {:href (str "/archives/")} "Archives"]])))

    (if (< 0 page)
      (.append 
       content 
       (html 
	[:div {:class "pager-right"}
	 [:a {:href (str "/latest-posts/" (- page 1) "/")} 
	  "Newer Entries &raquo;"]])))

    (.toString content)))

(defn- render-snippets [begin end]
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
