(ns app.tags
  (:use compojure)
  (:use clojure.set)
  (:use :reload-all [app.util :only [post-list-by-date file-to-url]])
  (:use :reload-all [app.template :only [render-template]])
  (:use :reload-all [app.markdown :only [read-markdown]]))

(def tags-page (ref ""))

(defn tag-post [post]
  (let [metadata (:metadata (read-markdown (str "posts/" post)))
	url      (file-to-url post)] 
    (reduce (fn 
	      [h v] 
	      (conj h { :tag v :post {:url url :title (metadata "title")}}))
    	    #{}
    	    (.split (metadata "tags") " "))  ))

(defn tag-list [tag tag-set]
  (let [posts (project (select #(= (:tag %) tag) tag-set ) [:post])]
    (reduce 
     (fn [h v] 
       (conj h [:li [:a {:href (:url (:post v))} (:title (:post v))]]  ))
     [:ul ] posts) ))

(defn tag-page-content [tag-set tag-distinct]
  (html
   (reduce 
    (fn [h v] 
      (conj h [:h4 (:tag v)] (tag-list (:tag v) tag-set)  ))
    [:div ] tag-set)))

(defn tags []
  (let [tag-set      (apply union (map tag-post (post-list-by-date)))
	tag-distinct (project tag-set [:tag])
	content      (tag-page-content tag-set tag-distinct)]
    (render-template 
     {:metadata {"title" "Tags" "layout" "default"}
      :content  (tag-page-content tag-set tag-distinct)})))

(dosync (ref-set tags-page (tags)))
