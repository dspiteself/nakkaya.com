(ns app.template
  (:use :reload-all [app.util :only [read-file]]))


(defn replace-content [content template]
  (.replaceAll template "\\{\\{.*?content.*?\\}\\}" content))

(defn replace-title [metadata template]
  (.replaceAll template "\\{\\{.*?page.title.*?\\}\\}" (metadata "title")))

(defn process-metadata [metadata page]
  (let [template (read-file 
		  (str "layouts/" (metadata "layout") ".html"))]
    (replace-title metadata template) ))

(defn render-template [page]
  (let [metadata (:metadata page)
	content (:content page)]
    (replace-content content (process-metadata metadata page)) ))
