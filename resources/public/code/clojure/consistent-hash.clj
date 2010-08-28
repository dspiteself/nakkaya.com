(ns consistent-hash.core
  (:use [clojure.contrib seq-utils])
  (:use [net-eval])
  (:import (java.security MessageDigest)))

(defn sha1 [obj]
  (let [bytes (.getBytes (with-out-str (pr obj)))] 
    (apply vector (.digest (MessageDigest/getInstance "SHA1") bytes))))

(defn hash-node [node replicas]
  (map #(hash-map (sha1 (str node %)) node) (range replicas)))

(defn add-node [circle node replicas]
  (apply merge circle (hash-node node replicas)))

(defn remove-node [circle node replicas]
  (apply dissoc circle (map first (map keys (hash-node node replicas)))))

(defn consistent-hash [nodes replicas]
  (reduce (fn[h v] (add-node h v replicas)) (sorted-map) nodes))

(defn tail-map [circle hash]
  (filter #(<= 0 (compare (key %) hash)) circle))

(defn lookup [circle obj]
  (if-not (empty? circle)
    (let [hash (sha1 obj)
	  tail-map (tail-map circle hash)] 
      (if (empty? tail-map)
      	(val (first circle))
      	(val (first tail-map))))))

(deftask init-cache []
  (def consistent-cache (ref {})))

(deftask get-from-cache [key]
  (@consistent-cache key))

(deftask add-to-cache[key val]
  (dosync (alter consistent-cache merge {key val})))

(defn remote-init [nodes]
  (doseq [[ip port] nodes]
    (net-eval [[ip port #'init-cache]])))

(defn remote-get [hash nodes key]
  (let [[ip port] (lookup hash key)] 
    (deref (first (net-eval [[ip port #'get-from-cache key]])))))

(defn remote-add [hash nodes key val]
  (let [[ip port] (lookup hash key)]
    (net-eval [[ip port #'add-to-cache key val]])))

(comment
  (def nodes [["127.0.0.1" 9999]
	      ["10.211.55.3" 9999]])

  (def cons-hash (consistent-hash nodes 3))

  (lookup cons-hash  "some_key")
  (lookup cons-hash  "some_other_key")

  (remote-init nodes)
  (remote-add cons-hash nodes "some_key" "42")
  (remote-get cons-hash nodes "some_key")
  )


