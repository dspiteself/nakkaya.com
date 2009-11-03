(comment 
  "THE BEER-WARE LICENSE (Revision 42):
  --------------------------------------
  <nurullah@nakkaya.com> wrote this file. 
  As long as you retain this notice you
  can do whatever you want with this stuff. 
  If we meet some day, and you think
  this stuff is worth it, you can buy me a beer 
  in return. 
  Nurullah Akkaya")

(ns bencode
  (:use clojure.test)
  (:import (java.io InputStream)
	   (java.io ByteArrayInputStream)))
(declare decode)

(defn- decode-number [stream delimeter & ch]
  (loop [i (if (nil? ch) (.read stream) (first ch)), result ""]
    (let [c (char i)]
      (if (= c delimeter)
	(BigInteger. result)
	(recur (.read stream) (str result c))))))

(defn- decode-string [stream ch]
  (let [length (decode-number stream \: ch)
	buffer (make-array Byte/TYPE length)]
    (.read stream buffer)
    (String. buffer "ISO-8859-1")))

(defn- decode-list [stream]
  (loop [result []]
    (let [c (char (.read stream))]
      (if (= c \e)
	result
	(recur (conj result (decode stream (int c))))) )))

(defn- decode-map [stream] 
  (apply hash-map (decode-list stream)))

(defn decode [stream & i]
  (let [indicator (if (nil? i) (.read stream) (first i))]
    (cond 
     (and (>= indicator 48) 
	  (<= indicator 57)) (decode-string stream indicator)
     (= (char indicator) \i) (decode-number stream \e)
     (= (char indicator) \l) (decode-list stream)
     (= (char indicator) \d) (decode-map stream))))

(defn- string-to-stream [code]
  (new ByteArrayInputStream (.getBytes code)))

(deftest test-decode
  (is (= 23  (decode (string-to-stream "i23e"))))
  (is (= -23 (decode (string-to-stream "i-23e"))))
  (is (= 0   (decode (string-to-stream "i0e"))))
  (is (= "strn2dsnf9" (decode (string-to-stream "10:strn2dsnf9"))))
  (is (= ["spam" ["spam" "eggs"]] 
	 (decode (string-to-stream "l4:spaml4:spam4:eggsee"))))
  (is (= ["spam" "eggs"] (decode (string-to-stream "l4:spam4:eggse"))))
  (is (= ["spam" 23] (decode (string-to-stream "l4:spami23ee"))))
  (is (= {"spam" "eggs", "cow" "moo"} 
	 (decode (string-to-stream "d3:cow3:moo4:spam4:eggse"))))
  (is (= {"spam" ["spam" "lk"], "cow" "moo"} 
	 (decode (string-to-stream "d3:cow3:moo4:spaml4:spam2:lkee")))))

(run-tests)

;; (let [torrent (decode (java.io.FileInputStream. (java.io.File. "test")))
;;       files ((torrent "info") "files")]
;;   (doseq [file files]
;;     (println (file "path") "-" (file "length")) ))

;; (doseq [torrent (decode (java.io.FileInputStream. (java.io.File. "test")))]
;;   (println (first torrent)) )
