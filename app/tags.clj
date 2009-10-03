(ns app.tags
  (:use compojure)
  (:use clojure.set)
  (:use :reload-all [app.util :only [post-list-by-date file-to-url]])
  (:use :reload-all [app.markdown :only [read-markdown]]))

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

(defn post-count-by-tags []
  (let [tag-set      (apply union (map tag-post (post-list-by-date)))]
    (reduce
     (fn [h v]
       (let [tag (:tag v)
	     count (get h tag 0)] 
	 (assoc h tag (+ 1 count) ))) {} tag-set) ))

(defn tag-page-content [tag-set tag-distinct]
  (html
   (reduce
    (fn [h v]
      (conj h [:h4 [:a {:name (:tag v)} (:tag v)]] 
	    (tag-list (:tag v) tag-set)  ))
    [:div ] tag-distinct)))

(defn tags-page []
  (let [tag-set      (apply union (map tag-post (post-list-by-date)))
	tag-distinct (project tag-set [:tag])
	content      (tag-page-content tag-set tag-distinct)]
    (tag-page-content tag-set tag-distinct)))
