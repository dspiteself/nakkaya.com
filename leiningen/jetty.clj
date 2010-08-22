(ns leiningen.jetty
  (:use clojure.contrib.java-utils))

(defn bash [p] 
  (.. Runtime getRuntime (exec (into-array ["/bin/bash" "-c" p]))))

(defn class-path []
  (apply str "-cp " (interpose  ":" ["./lib/*" "./src/"])))

(defn jvm-opts [project opts]
  (apply str 
	 "-Dfile.encoding=\"UTF-8\" "
	 "-Dcompojure.site=\""(:group project)"\" "
	 (map #(str " -Dcompojure." % "=\"true\" ") opts)))

(defn start-cmd [project opts]
  (let [pid-file "./pid"
	java-cmd "nohup java "
	clj-cmd " clojure.main "
	core-file (str (:source-path project) "/" 
		       (:group project) "/core.clj")]
    (if-not (.exists (file pid-file))
      (do (println "Starting Jetty...")
	  (bash (str java-cmd (jvm-opts project opts) " " (class-path)
		     clj-cmd core-file "&" "echo $! > pid")))
      (println "Jetty is running.."))))

(defn stop-cmd []
  (let [pid-file "./pid"] 
    (if (.exists (file pid-file))
      (do (println "Stopping Jetty..")
	  (bash (str "kill -9 " (slurp pid-file)))
	  (delete-file (file pid-file)))
      (println "Jetty is not running.."))))

(defn jetty [project & args] 
  (if-let [[cmd & opts] args] 
    (if (= cmd "start")
      (start-cmd project opts)
      (stop-cmd))
    (println "Usage: lein jetty {start|stop} [opts]")))
