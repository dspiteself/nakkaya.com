(ns robocup.core
  (:import (java.net Socket)
	   (java.nio ByteBuffer)))

(def joints 
     {:shoulder {:type :hinge 
		 :perceptor {:left :laj1 :right :raj1} 
		 :efector   {:left :lae1 :right :rae1}}
      :upper-arm {:type :hinge 
		  :perceptor {:left :laj2 :right :raj2} 
		  :efector   {:left :lae2 :right :rae2}}})

(defn connect [ip port]
  (let [socket (Socket. ip port)
	out (.getOutputStream socket)
	in (.getInputStream socket)]
    {:socket socket :in in :out out :out-agent (agent "")}))

(defn write-msg [conn msg]
  (let [{out :out agent :out-agent} conn
	write (fn [_]
		(try
		 (doto out
		   (.write (-> (ByteBuffer/allocate 4) 
			       (.putInt (count msg)) .array))
		   (.write (.getBytes msg))
		   (.flush))
		 (catch Exception e "")))]
    (send agent write)))

(defn msg-length [conn]
  (let [buf (ByteBuffer/allocate 4)] 
    (doseq [i (range 4)] (.put buf (byte (.read (:in conn)))))
    (.getInt buf 0)))

(defn read-msg [conn]
  (let [length (msg-length conn)
	buffer (byte-array length)]
    (.read (:in conn) buffer 0 length)
    (read-string (str "(" (apply str (map char buffer)) ")"))))

(defn close [conn]
  (.close (:in conn))
  (.close (:out conn))
  (.close (:socket conn)))

(defmacro forever [& body] 
  `(try (while true  ~@body) (catch Exception e#)))

(defn in-thread [f]
  (doto (Thread. f)
    (.start)))

(defn hinge-joints [msg]
  (reduce (fn[h v] 
	    (let [[_ [_ n] [_ ax]] v] (assoc h (keyword n) ax)))
	  {} (filter #(= (first %) 'HJ) msg)))

(defn universal-joints [msg]
  (reduce (fn[h v] 
	    (let [[_ [_ n] [_ ax1] [_ ax2]] v]
	      (assoc h (keyword n) [ax1 ax2])))
	  {} (filter #(= (first %) 'UJ) msg)))

(defn player [ip port]
  (let [conn (connect ip port)
	hjoints (ref {})
	ujoints (ref {})]
    (write-msg conn "(scene rsg/agent/nao/nao.rsg)")
    (in-thread 
     #(forever 
       (let [msg (read-msg conn)]
	 (dosync
	  (alter hjoints merge (hinge-joints msg))
	  (alter ujoints merge (universal-joints msg))))))
    {:conn conn :hinge-joints hjoints :universal-joints ujoints}))

(defn direction [player perceptor angle]
  (let [target-angle (+ 200 angle)
	current-angle (+ 200 (perceptor @(:hinge-joints player)))]
    (if (> target-angle current-angle) 1 -1)))

;;(direction {:hinge-joints (ref {:raj1 1.12})} :raj1 40)
;;(direction {:hinge-joints (ref {:raj1 -1.12})} :raj1 40)
;;(direction {:hinge-joints (ref {:raj1 1.12})} :raj1 -40)
;;(direction {:hinge-joints (ref {:raj1 -1.12})} :raj1 -40)

(defn in-range? [val target error]
  (some true? (map #(= (int val) %) 
		   (range (- target error) (+ target error)))))

;;(in-range? 2 10 5)

(defn move-joint [player start-cmd stop-cmd angle perceptor]
  (future 
   (write-msg (:conn player) start-cmd)
   (while (not (in-range? (perceptor @(:hinge-joints player)) angle 5)))
   (write-msg (:conn player) stop-cmd)
   (println perceptor (perceptor @(:hinge-joints player)))))

(defn command [player side j angle]
  (let [joint (j joints)
	perceptor (side (:perceptor joint))
	effector (side (:efector joint))
	dir (direction player perceptor angle)
	start-cmd (str (list (symbol (name effector)) dir))
	stop-cmd (str (list (symbol (name effector)) 0))]
    (move-joint player start-cmd stop-cmd angle perceptor)))

(comment
  (def nao-bot (player "127.0.0.1" 3100))

  ;;wave
  (do
    (command nao-bot :right :shoulder -90)
    @(command nao-bot :left :shoulder 60)
    (doseq [i (range 3)]
      @(command nao-bot :left :upper-arm 50)
      @(command nao-bot :left :upper-arm 0))
    (command nao-bot :left :shoulder -90))

  (close (:conn nao-bot))
  )
