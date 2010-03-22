(ns steganography
  (:use clojure.contrib.seq-utils)
  (:import (javax.imageio ImageIO)
	   (java.io File)))

(defn bits [n]
  (reverse (map #(bit-and (bit-shift-right n %) 1) (range 8))))

(defn numb [bits]
  (BigInteger. (apply str bits) 2))

(defn set-lsb [bits bit]
  (concat (take 7 bits) [bit]))

(defn string-to-bits [msg]
  (flatten (map #(bits %) (.getBytes (str msg ";")))))

(defn get-argb [img cord]
  (let [[x y] cord
	clr (.getRGB img x y)]
    [(bit-and (bit-shift-right clr 24) 0xff)
     (bit-and (bit-shift-right clr 16) 0xff)
     (bit-and (bit-shift-right clr 8) 0xff)
     (bit-and clr 0xff)]))

(defn set-argb [img cord color]
  (let [[x y] cord
	[a r g b] color
	c (bit-or (bit-shift-left a 24)
		  (bit-or (bit-shift-left r 16)
			  (bit-or (bit-shift-left g 8) b)))]
    (.setRGB img x y c)))

(defn match-bits-coords [bits img]
  (partition 2 
	     (interleave (partition 4 bits)
			 (take (/ (count bits) 4) 
			       (for [x (range (.getWidth img)) 
				     y (range (.getHeight img))] [x y])))))
(defn set-pixels [img d]
  (doseq [[data cord] d]
    (let [color-bit (partition 2 (interleave (get-argb img cord) data))
	  color (map #(let [[n b] %]
			(numb (set-lsb (bits n) b))) color-bit)]
      (set-argb img cord color))))

(defn encode [fname msg]
  (let [img (ImageIO/read (File. fname))
	data (match-bits-coords (string-to-bits msg) img)]
    (set-pixels img data)
    (ImageIO/write img "png" (File. (str "encoded_" fname)))))

;;(encode "drive.png" "Attack At Down!!")

(defn get-pixels [img]
  (map #(get-argb img %) (for [x (range (.getWidth img)) 
			       y (range (.getHeight img))] [x y])))

(defn split-lsb [data]
  (map #(last (bits %)) data))

(defn decode [fname]
  (let [img (ImageIO/read (File. fname))
	to-char #(char (numb (first %)))]
    (loop [bytes (partition 8 (split-lsb (flatten (get-pixels img))))
    	   msg (str)]
      (if (= (to-char bytes) \;)
    	msg
    	(recur (rest bytes) (str msg (to-char bytes)))))))

;;(decode "encoded_drive.png")
