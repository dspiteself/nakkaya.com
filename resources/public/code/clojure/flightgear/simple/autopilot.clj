(ns autopilot.core
  (:use clojure.contrib.swing-utils)
  (:import (javax.swing JFrame JButton)
	   (java.net InetAddress DatagramSocket DatagramPacket)))

(def fg-host (InetAddress/getByName "127.0.0.1"))
(def fg-port-out 6666)
(def fg-port-in 6789)

(defn in-thread [f] (doto (Thread. f) (.start)))

(defn map-number [x in-min in-max out-min out-max]
  (let [val (+ (/ (* (- x in-min)
		     (- out-max out-min))
		  (- in-max in-min)) out-min)]
    (cond (> val out-max) out-max
	  (< val out-min) out-min
	  :default val)))

(defn controller [roll pitch]
  (let [roll-cntrl (float (map-number roll 90 -90 -1 1))
	pitch-cntrl (float (map-number pitch -45 45 -1 1))]
    (println "Control: " roll roll-cntrl pitch pitch-cntrl)
    [roll-cntrl pitch-cntrl]))

(defn control-loop [active]
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
		[roll-cntrl pitch-cntrl] (apply controller state)]
	    (.setLength packet-in (count buffer-in))
	    (let [msg (.getBytes (str roll-cntrl \, pitch-cntrl "\n"))
		  packet (DatagramPacket. msg (count msg)
					  fg-host fg-port-in)]
	      (.send socket-out packet))))
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
	     (control-loop active))
	 (do (.setText button "Autopilot OFF")
	     (dosync (ref-set active false))))))
    (doto (JFrame.)
      (.add button)
      (.pack)
      (.setVisible true))))


;;cd /Applications/FlightGear.app/Contents/Resources/
;;./fgfs.sh --timeofday=morning --aircraft=c172p-2dpanel --shading-flat --disable-textures --geometry=640x480 --fog-disable --disable-horizon-effect --disable-clouds --generic=socket,out,40,localhost,6666,udp,output-protocol --generic=socket,in,45,127.0.0.1,6789,udp,input-protocol
