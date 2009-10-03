(ns app.template
  (:use compojure)
  (:use :reload-all [app.util :only [read-file]])
  (:use :reload-all [app.post :only [post-count-by-mount]]))

(def content-regex "\\{\\{.*?content.*?\\}\\}")
(def title-regex "\\{\\{.*?page.title.*?\\}\\}")
(def tags-regex "\\{\\{.*?tags.*?\\}\\}")
(def description-regex "\\{\\{.*?description.*?\\}\\}")
(def preprocess-regex "\\{\\{.*?\\}\\}")
(def post-count-by-mount-regex "\\{\\{.*?post.count.by.mount.*?\\}\\}")

(defn replace-post-cost-by-mount [page]
  (let [months  (post-count-by-mount)] 
    (.replaceAll 
     page post-count-by-mount-regex
     (html
      [:h5 "Archives"]
      (reduce (fn [h v]
		(conj h [:li [:a {:href (key v)} (key v)] " (" (val v)")"]))
	      [:ul] months))
    )))

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
	    #(replace-post-cost-by-mount %)
	    #(replace-content content %)
	    #(replace-description metadata %)
	    #(replace-tags metadata %)
	    #(replace-title metadata template) ))  ))
