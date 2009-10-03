(ns app.markdown
  (:use :reload-all clojure.contrib.str-utils)
  (:use :reload-all [app.util :only [read-file]])
  (:import (com.petebevin.markdown MarkdownProcessor)))

(def markdown-processor (MarkdownProcessor.))

(defn render-markdown [txt]
  (.markdown markdown-processor txt))

(defn split-file 
  "split file into metadata and content"
  [content]
  (let [split-index (.indexOf content "---" 4)
	metadata (.substring content 4 split-index)
	content  (.substring content   (+ 3 split-index))] 
    {:metadata metadata :content content} ))

(defn prepare-metadata [metadata]
  (reduce (fn [h [_ k v]] (assoc h k v))
	  {}
	  (re-seq #"([^:]+): (.+)(\n|$)" metadata)))

(defn read-markdown [file]
  (let [content (read-file file)
	page (split-file content)
	metadata (prepare-metadata (:metadata page))
	html (render-markdown (:content page))]
    {:metadata metadata :content html} ))
