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

(ns tracker
  (:use :reload-all bencode)
  (:use clojure.test)
  (:import (java.security MessageDigest)
	   (java.io FileInputStream File 
		    BufferedReader InputStreamReader ByteArrayInputStream)
	   (java.net URL)))

(def peer-id "ABCDEFGHIJKLMNOPQRST")
(def port-in 6881)
(def peer-list-size 100)

(defn calc-info-hash [torrent]
  (let [info (encode (torrent "info"))
	sha1 (MessageDigest/getInstance "SHA1")
	hash (.toString (BigInteger. (.digest sha1 info)) 16)
	pad (- 40 (count hash))]
    (str (apply str (take pad (repeat "0"))) hash)))

(defn url-encode [hash]
  (apply str
	 (map (fn [[a b]] 
		(let [byte (BigInteger. (str a b)  16) ]
		  (if (or (and (>= byte 65) (<= byte 90)) ; A-Z
			  (and (>= byte 97) (<= byte 122)) ; a-z
			  (and (>= byte 48) (<= byte 57)) ; 0-9
			  (= byte 45) (= byte 95) (= byte 46) (= byte 126))
		    (char byte) (str "%" a b)) )) (partition 2 hash))))

(defn build-request [torrent]
  (let [announce (torrent "announce")
	hash (calc-info-hash torrent)
	event "started"]
    (str announce "?" 
	 "info_hash=" (url-encode hash) "&"
	 "peer_id=" peer-id "&" "port=" port-in "&"
	 "uploaded=0&downloaded=0&" "left=" ((torrent "info") "length") "&"
	 "event=" event "&" "numwant=" peer-list-size "&compact=1" )))

(defn request [address]
  (let  [ url (URL. address)] 
    (with-open [stream (.openStream url)]
      (let  [ buf (BufferedReader. (InputStreamReader. stream "ISO-8859-1"))]
	(apply str (line-seq buf))))))

(defn peers [peers]
  (reduce (fn[list peer]
	    (conj list 
		  {:ip (apply str (interpose \. (map int (take 4 peer))))
		   :port (+ (* 256 (int (nth peer 5))) (int (nth peer 4)))}))
	  [] (partition 6 peers)))

(defn get-torrent-stats [fname]
  (let [torrent (decode (FileInputStream. (File. fname)))
	request (request (build-request torrent))
	stats (decode (ByteArrayInputStream. (.getBytes request)))]
    {:complete (stats "complete") 
     :incomplete (stats "complete") 
     :peers (peers (stats "peers"))}))

;(get-torrent-stats "buntu.torrent")

(deftest test-hash
  (is (= "3e427a0a9d4826e76cd5363a004b2fa6baca1853"  
	 (calc-info-hash {"info" {"length" 3 "piece length" 54}})))
  (is (= "%124Vx%9a%bc%de%f1%23Eg%89%ab%cd%ef%124Vx%9a"
	 (url-encode "123456789abcdef123456789abcdef123456789a"))))

(deftest test-peers
  (is (= [{:ip "122.122.122.122", :port 31354}]
	 (peers [\z \z \z \z \z \z])))
  (is (= [{:ip "85.86.88.83", :port 31354}]
	 (peers [\U \V \X \S \z \z]))))

(run-tests)
