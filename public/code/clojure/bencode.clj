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
	   (java.io ByteArrayInputStream ByteArrayOutputStream)))
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Encoding
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(declare encode-object)

(defn- encode-string [obj stream]
  (let [bytes (.getBytes obj "UTF-8")
	bytes-length (.getBytes (str (count bytes) ":") "UTF-8")]
    (.write stream bytes-length 0 (count bytes-length))
    (.write stream bytes 0 (count bytes)) ))

(defn- encode-number [number stream]
  (let [string (str "i" number "e")
	bytes (.getBytes string "UTF-8")]
    (.write stream bytes 0 (count bytes)) ))

(defn- encode-list [list stream]
  (.write stream (int \l))
  (doseq [item list]
    (encode-object item stream))
  (.write stream (int \e)))

(defn- encode-dictionary [dictionary stream]
  (.write stream (int \d))
  (doseq [item dictionary]
    (encode-object (first item) stream)
    (encode-object (second item) stream))
  (.write stream (int \e)))

(defn- encode-object [obj stream]
  (cond (string?  obj) (encode-string obj stream)
	(number? obj) (encode-number obj stream)
	(vector? obj) (encode-list obj stream)
	(map? obj) (encode-dictionary obj stream)))

(defn encode [obj]
  (let [stream (ByteArrayOutputStream.)] 
    (encode-object obj stream)
    (.toByteArray stream)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- byte-stream [array]
  (new ByteArrayInputStream array))

(defn- string-to-stream [code]
  (new ByteArrayInputStream (.getBytes code)))

(deftest test-encode
  (is (= "strn2dsnf9"  (decode (byte-stream (encode "strn2dsnf9")))))
  (is (= 24  (decode (byte-stream (encode 24)))))
  (is (= ["spam" "eggs"] (decode (byte-stream (encode ["spam" "eggs"])))))
  (is (= ["spam" ["spam" "eggs"]] 
	 (decode (byte-stream (encode ["spam" ["spam" "eggs"]])))))
  (is (= {"spam" "eggs", "cow" "moo"}
	 (decode (byte-stream (encode {"spam" "eggs", "cow" "moo"})))))
  (is (= 
       {"spam" ["spam" "lk"], "cow" "moo"}
       (decode (byte-stream (encode {"spam" ["spam" "lk"], "cow" "moo"}))))))

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
