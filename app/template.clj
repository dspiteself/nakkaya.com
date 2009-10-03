(ns app.template
  (:use compojure)
  (:use :reload-all [app.util :only [read-file]])
  (:use :reload-all [app.post :only [post-count-by-mount]])
  (:use :reload-all [app.tags :only [post-count-by-tags]])
  (:import (java.text SimpleDateFormat)))

(def content-regex "\\{\\{.*?content.*?\\}\\}")
(def title-regex "\\{\\{.*?page.title.*?\\}\\}")
(def tags-regex "\\{\\{.*?tags.*?\\}\\}")
(def description-regex "\\{\\{.*?description.*?\\}\\}")
(def preprocess-regex "\\{\\{.*?\\}\\}")
(def post-count-by-mount-regex "\\{\\{.*?post.count.by.mount.*?\\}\\}")
(def post-count-by-tags-regex "\\{\\{.*?post.count.by.tags.*?\\}\\}")

(defn format-time [time]
  (let  [parse-format (SimpleDateFormat. "yyyy-MM")
	 date (.parse parse-format time)
	 print-format (SimpleDateFormat. "MMMM yyyy")]
    (.format print-format date)))

(defn replace-post-cost-by-mount [page]
  (let [months  (post-count-by-mount)] 
    (.replaceAll 
     page post-count-by-mount-regex
     (html
      [:h5 "Archives"]
      (reduce (fn [h v]
		(conj h [:li [:a {:href (key v)} (format-time (key v))] 
			 " (" (val v)")"]))
	      [:ul] months))  )))

(defn replace-post-cost-by-tags [page]
  (let [tags  (post-count-by-tags)] 
    (.replaceAll 
     page post-count-by-tags-regex
     (html
      [:h5 "Tags"]
      (reduce (fn [h v]
		(conj h [:li [:a {:href (str "/tags/#" (key v))} (key v)]
			 " (" (val v)")"]))
	      [:ul] tags)) )))

(defn replace-content [content page]
  (.replaceAll page content-regex content))

(defn replace-tags [metadata page]
  (if (not(nil? (metadata "tags")))
    (.replaceAll page tags-regex (metadata "tags"))
    page))

(defn replace-description [metadata page]
  (if (not(nil? (metadata "description")))
    (.replaceAll page description-regex (metadata "description"))
    page))

(defn replace-title [metadata page]
  (.replaceAll page title-regex (metadata "title")))

(defn replace-preprocess [content]
  (.replaceAll content preprocess-regex ""))

(defn render-template [page]
  (let [metadata (:metadata page)
	content  (:content page)
	template (read-file (str "layouts/" (metadata "layout") ".html"))]

    ((comp  #(replace-preprocess %)
	    #(replace-description metadata %)
	    #(replace-tags metadata %)
	    #(replace-post-cost-by-mount %)
	    #(replace-post-cost-by-tags %)
	    #(replace-content content %)
	    #(replace-title metadata template) ))  ))
