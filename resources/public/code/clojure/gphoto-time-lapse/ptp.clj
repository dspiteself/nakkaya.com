(ns ptp.core
  (:use clojure.java.io)
  (:import (com.sun.jna Function Pointer)))

(System/setProperty "jna.library.path" "./")

(defn ptp [func ret & args]
  (let [f (Function/getFunction "ptp_lib" (name func))]
    (.invoke f ret (to-array args))))

(let [camera (delay (ptp :ptp_init Pointer))
      index (atom 1)
      running (atom true)]
  
  (defn start []
    (let [out-dir (file "prevs")]
      (if (not (.exists out-dir))
	(.mkdir out-dir))
      (ptp :extend_lens Integer @camera)
      (.start (Thread. (fn []
			 (while @running
			   (let [f (str "prevs/preview-" @index ".jpg")]
			     (ptp :preview Integer @camera f))
			   (swap! index inc)
			   (Thread/sleep 15000)))))))
  
  (defn stop []
    (swap! running not)
    (ptp :retract_lens Integer @camera)
    (ptp :ptp_exit Integer @camera)))
