(ns app.template
  (:use compojure)
  (:use :reload-all app.storage)
  (:use :reload-all [app.markdown :only [read-markdown]])
  (:import  (java.text SimpleDateFormat)
	    (java.util Properties)
	    (java.io StringWriter)
	    (org.apache.velocity.app Velocity VelocityEngine) 
	    (org.apache.velocity VelocityContext Template)  ))

(defn velocity-init []
  (let [engine (VelocityEngine.)
	properties (Properties.)]
    (.setProperty properties 
		  "runtime.log.logsystem.class" 
		  "org.apache.velocity.runtime.log.NullLogSystem")
    (.init engine properties)
    engine))

(defn render-template [page]
  (let  [engine  (velocity-init)
	 content (:content page)
	 metadata (:metadata page)
	 context (VelocityContext.)
	 template-file (str "layouts/" (metadata "layout") ".xml")
	 template (.getTemplate engine template-file)
	 writer (StringWriter.)]

    (doseq [value metadata]
      (.put context (first value) (second value)))

    (.put context "content" content)

    (.put context "post-count-by-tags" (post-count-by-tags))

    (.put context "dateIn" (SimpleDateFormat. "yyyy-MM"))
    (.put context "dateOut" (SimpleDateFormat. "MMMM yyyy"))
    (.put context "post-count-by-mount" (post-count-by-mount))

    (.merge template context writer)
    (.toString writer) ))

(defn render-page [file]
  (let  [content (read-markdown file)]
    (render-template content)))
