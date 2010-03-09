;;http://upload.wikimedia.org/wikipedia/commons/9/9b/MapTurkishProvincesNumbers.svg
(ns worldplot.core
  (:use [incanter core processing]))

;; Data for 2009
(def pop-taken [{:id 1 :name "Marmara" :population 582771}
		{:id 2 :name "Iç Anadolu" :population 297919}
		{:id 3 :name "Ege" :population 164896}
		{:id 4 :name "Akdeniz" :population 188441}
		{:id 5 :name "Karadeniz" :population 256654}
		{:id 6 :name "Güneydoğu Anadolu" :population 171910}
		{:id 7 :name "Doğu Anadolu" :population 214082}])

(def pop-given [{:id 1 :name "Marmara" :population 677395}
		{:id 2 :name "Iç Anadolu" :population 310293}
		{:id 3 :name "Ege" :population 181459}
		{:id 4 :name "Akdeniz" :population 193231}
		{:id 5 :name "Karadeniz" :population 247397}
		{:id 6 :name "Güneydoğu Anadolu" :population 118611}
		{:id 7 :name "Doğu Anadolu" :population 148287}])

(defn region-color [val min max]
  (lerp-color (color 0xffd120) (color 0x920903) (norm val min max)))

(defn map-region-color [regions]
  (let [min (apply min (map #(:population %) regions))
	max (apply max (map #(:population %) regions))]
    (map #(vector (:id %) (region-color (:population %) min max)) regions)))

(defn sktch [regions]
  (sketch
   (setup [])
   (draw 
    []
    (let [tr-map (load-shape this "MapTurkishProvincesNumbers.svg")]
      (.shape this tr-map 0 0)
      (doseq [region (map-region-color regions)]
	(let [[id color] region
	      child (.getChild tr-map (str id))]
	  (.disableStyle child)
	  (.fill this color)
	  (.noStroke this)
	  (.shape this child 0 0)
	  no-loop))))))

;;(view (sktch pop-given) :size [1052 744])
;;(view (sktch pop-taken) :size [1052 744])
