(defproject nakkaya "6.6.6"
  :description "nakkaya.com"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
		 [org.clojars.nakkaya/markdownj "1.0.2b4"]
		 [compojure "0.4.0-SNAPSHOT"]
		 [ring/ring-devel "0.2.0-RC2"]
		 [ring/ring-httpcore-adapter "0.2.0-RC2"]
		 [ring/ring-jetty-adapter "0.2.0-RC2"]
		 [ring/ring-servlet "0.2.0-RC2"]
		 [hiccup "0.2.1"]])

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
