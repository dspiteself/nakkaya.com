(ns autopilot.core
  (:use clojure.contrib.prxml)
  (:use clojure.contrib.swing-utils)
  (:import (javax.swing JFrame JButton)
	   (java.net InetAddress DatagramSocket DatagramPacket)))

(def fg-host (InetAddress/getByName "127.0.0.1"))
(def fg-port-out 6666)
(def fg-port-in 6789)

(defn in-thread [f] (doto (Thread. f) (.start)))

(defn scale [x in-min in-max out-min out-max]
  (+ (/ (* (- x in-min) (- out-max out-min)) (- in-max in-min)) out-min))

(defn clamp [x min max]
  (cond
   (> x max) max
   (< x min) min
   :default x))

(defn pid
  ([s]
     (ref (assoc s :integrator 0 :derivator 0)))
  ([s v]
     (let [{:keys [set-point kp kd ki integrator derivator bounds]} @s
	   [in-min in-max out-min out-max] bounds
	   v (scale (clamp v in-min in-max) in-min in-max -1.0 1.0)
	   sp (scale (clamp set-point in-min in-max) in-min in-max -1.0 1.0)
	   error (- sp v)
	   p-val (* kp error)
	   d-val (* kd (- error derivator))
	   integrator (clamp (+ integrator error) -1.0 1.0)
	   i-val (* integrator ki)
	   pid (scale (clamp (+ p-val i-val d-val) -1.0 1.0)
		      -1.0 1.0 out-min out-max)]
       (dosync (alter s assoc :integrator integrator :derivator error))
       pid)))

(let [p (pid {:kp 4
	      :ki 0
	      :kd 0
	      :set-point 80
	      :bounds [0 120 0 1]})]
  (defn speed-hold [curr]
    (double (pid p curr))))

(let [p (pid {:kp 2
	      :ki 1/10
	      :kd 0
	      :set-point 0
	      :bounds [-180 180 -1 1]})]
  (defn set!-roll [ang]
    (dosync (alter p assoc :set-point ang)))
  (defn roll-hold [curr]
    (double (pid p curr))))

(let [p (pid {:kp 4
	      :ki 1/5
	      :kd 0
	      :set-point 0
	      :bounds [-90 90 -1 1]})]
  (defn set!-pitch [ang]
    (dosync (alter p assoc :set-point ang)))
  (defn pitch-hold [curr]
    (* (double (pid p curr)) -1)))

(let [p (pid {:kp 15
	      :ki 1/2
	      :kd 0.0
	      :set-point 1000
	      :bounds [0 10000 -10 10]})]
  (defn set!-altitude [alt]
    (dosync (alter p assoc :set-point alt)))
  (defn altitude-hold [curr]
    (set!-pitch (int (pid p curr)))))

(let [p (pid {:kp 2
	      :ki 0.0
	      :kd 0.0
	      :set-point 90
	      :bounds [0 180 -10 10]})
      norm-ang #(if (and (>= % 180)
			 (<= % 360))
		  (clamp (scale % 270 360 0 90) 0 90)
		  (clamp (scale % 0 90 90 180) 90 180))]
  (defn set!-heading [h]
    (dosync (alter p assoc :set-point (norm-ang h))))
  (defn heading-hold [curr]
    (set!-roll (int (pid p (norm-ang curr))))))

(defn bearing [c1 c2]
  (let [[lat1 lon1] (map #(Math/toRadians %) c1)
	[lat2 lon2] (map #(Math/toRadians %) c2)]
    (Math/toDegrees
     (mod (Math/atan2
	   (* (Math/sin (- lon2 lon1)) (Math/cos lat2))
	   (- (* (Math/cos lat1)
		 (Math/sin lat2))
	      (* (Math/sin lat1) 
		 (Math/cos lat2)
		 (Math/cos (- lon2 lon1)))))
	  (* 2 Math/PI)))))

(defn distance [c1 c2]
  (let [[lat1 lon1] (map #(Math/toRadians %) c1)
	[lat2 lon2] (map #(Math/toRadians %) c2)]
    (* 2 6371.0
       (Math/asin
	(Math/sqrt
	 (+ (Math/pow (Math/sin (/ (- lat1 lat2) 2)) 2)
	    (* (Math/cos lat1)
	       (Math/cos lat2)
	       (Math/pow (Math/sin (/ (- lon1 lon2) 2)) 2))))))))

(let [route (ref [[38.702803 33.454353]
		  [38.756064 33.209744]
		  [38.908678 33.296394]])]
  (defn dist-to-wp [lat long]
    (distance [lat long] (first @route)))
  (defn guidance [heading alt lat long]
    (when (> alt 400)
      (let [wp (first @route)
	    dist (distance [lat long] wp)]
	(set!-heading (bearing [lat long] wp))
	(heading-hold heading)
	(if (and (< dist 1.0)
		 (not (empty? (rest @route))))
	  (dosync (ref-set route (rest @route))))))))

(let [path (ref [])]
  (defn add!-point [long lat]
    (dosync (alter path conj [long lat])))
  (defn dump-log []
    (spit "path.kml"
	  (with-out-str
	    (prxml
	     [:decl! {:version "1.0"}]
	     [:kml  {:xmlns "http://www.opengis.net/kml/2.2"}
	      [:Document
	       [:name "Flight Path"]
	       [:Style {:id "yellowLineGreenPoly"}
		[:LineStyle
		 [:color "7f00ffff"]
		 [:width 4]]]
	       [:Placemark
		[:name "WP-1"]
		[:Point
		 [:coordinates "33.454353,38.702803"]]] ;;long/lat
	       [:Placemark
		[:name "WP-2"]
		[:Point
		 [:coordinates "33.209744,38.756064"]]]
	       [:Placemark
		[:name "WP-3"]
		[:Point
		 [:coordinates "33.296394,38.908678"]]]

	       [:Placemark
		[:name "Path"]
		[:styleUrl "#yellowLineGreenPoly"]
		[:LineString
		 [:coordinates
		  (map #(let [[long lat] %]
			  (str long "," lat ", 0.\n")) @path)]]]]])))))

;;(dump-log)

(defn controller [[roll pitch alt heading air-speed lat long]]
  (add!-point long lat)
  (altitude-hold alt)
  (guidance heading alt lat long)
  (let [roll-cntrl (roll-hold roll)
	pitch-cntrl (pitch-hold pitch)
	speed-cntrl (speed-hold air-speed)]
    (println
     (format "R %1$.2f %2$.2f" roll roll-cntrl)
     (format "P %1$.2f %2$.2f" pitch pitch-cntrl)
     (format "S %1$.2f %2$.2f" air-speed speed-cntrl)
     (format "A %1$.2f" alt)
     (format "H %1$.2f" heading)
     (format "D %1$.2f" (dist-to-wp lat long)))
    [roll-cntrl pitch-cntrl speed-cntrl]))

(defn control-loop [active fn-call]
  (let [socket-in (DatagramSocket. fg-port-out)
	buffer-in (byte-array 2048)
	packet-in (DatagramPacket. buffer-in (count buffer-in))
	socket-out (DatagramSocket.)]
    (in-thread
     #(try
	(while @active
	  (.receive socket-in packet-in)
	  (let [state (read-string
		       (String. buffer-in 0 (dec (.getLength packet-in))))
		cntrl (fn-call state)
		msg (str (apply str (interpose \, cntrl)) "\n")
		buf (.getBytes msg)
		packet (DatagramPacket. buf (count buf) fg-host fg-port-in)]
	    (.setLength packet-in (count buffer-in))
	    (.send socket-out packet)))
	(finally (.close socket-in)
		 (.close socket-out))))))

(defn autopilot []
  (let [active (ref false)
	button (JButton. "Autopilot OFF")]
    (.setFont button (-> button .getFont (.deriveFont (float 40))))
    (add-action-listener
     button
     (fn [_]
       (if (= false @active)
	 (do (.setText button "Autopilot ON")
	     (dosync (ref-set active true))
	     (control-loop active controller))
	 (do (.setText button "Autopilot OFF")
	     (dosync (ref-set active false))))))
    (doto (JFrame.)
      (.add button)
      (.pack)
      (.setVisible true))))


;;cd /Applications/FlightGear.app/Contents/Resources/
;;./fgfs.sh --timeofday=morning --aircraft=c172p-2dpanel --shading-flat --disable-textures --geometry=640x480 --fog-disable --disable-horizon-effect --disable-clouds --generic=socket,out,40,localhost,6666,udp,output-protocol --generic=socket,in,45,127.0.0.1,6789,udp,input-protocol --lat=38.670019 --lon=33.326933
