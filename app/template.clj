(ns app.template
  (:use :reload-all [app.util :only [read-file]]))

(def content-regex "\\{\\{.*?content.*?\\}\\}")
(def title-regex "\\{\\{.*?page.title.*?\\}\\}")
(def tags-regex "\\{\\{.*?tags.*?\\}\\}")
(def description-regex "\\{\\{.*?description.*?\\}\\}")
(def preprocess-regex "\\{\\{.*?\\}\\}")

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
	    #(replace-content content %)
	    #(replace-description metadata %)
	    #(replace-tags metadata %)
	    #(replace-title metadata template) ))  ))
