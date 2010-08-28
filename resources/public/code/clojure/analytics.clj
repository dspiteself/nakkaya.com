(ns analytics.core
  (:use incanter.core)
  (:use incanter.charts)
  (:import (java.net URL)
	   (com.google.gdata.client.analytics AnalyticsService DataQuery)))

(defn get-class [class]
  (Class/forName (str "com.google.gdata.data.analytics." class)))

(defn service [username pass]
  (doto (AnalyticsService. "Clojure_Incanter_Sample")
    (.setUserCredentials username pass)))

(defn account-feed [service & args]
  (let [url (URL. (str "https://www.google.com/analytics/"
		       "feeds/accounts/default?max-results=50"))
	feed (.getFeed service url (get-class "AccountFeed"))
	accs (reduce #(assoc %1 
			(-> %2 .getTitle .getPlainText)
			(-> %2 .getTableId .getValue)) 
		     {} (.getEntries feed))]
    (if (nil? args) accs (accs (first args)))))

(defn query [args]
  (let [ga-str (fn[s] (apply str (interpose "," (map #(str "ga" %) s))))]
    (doto (DataQuery. (URL. "https://www.google.com/analytics/feeds/data"))
      (.setStartDate (first (:date args)))
      (.setEndDate (last (:date args)))
      (.setDimensions (ga-str (:dimensions args)))
      (.setMetrics (ga-str (:metrics args)))
      (.setSort (str "-" (ga-str (:sort args))))
      (.setMaxResults (:num-result args))
      (.setIds (:id args)))))

(defn data-feed [service & args]
  (let [args (apply hash-map args)
	feed (.getFeed service (.getUrl (query args)) (get-class "DataFeed"))
	cols (map #(str "ga" %) (concat (:dimensions args) (:metrics args)))]
    (map (fn [e]
	   (map #(.stringValueOf e %) cols))
	 (.getEntries feed))))

(comment

  (def analytics (service "username" "pass"))
  (def acc-nakkaya (account-feed analytics "nakkaya.com"))
  (def acc-feed (account-feed analytics))

  (def pageview (data-feed analytics 
			   :date ["2010-01-26" "2010-02-25"]
			   :dimensions [:pageTitle :pagePath]
			   :metrics [:pageviews]
			   :sort [:pageviews]
			   :num-result 10
			   :id acc-nakkaya))
  
  (view pageview)
  (view (col-names (to-dataset pageview) [:title :path :views]))

  (with-data (col-names (map (fn [[x y z]] [x y (BigInteger. z)]) pageview)
			[:title :path :views])
    (view ($where {:views {:$gt 200 :$lt 800}})))

  (def keywords (data-feed analytics 
			   :date ["2010-01-26" "2010-02-25"]
			   :dimensions [:keyword]
			   :metrics [:visits]
			   :sort [:visits]
			   :num-result 10
			   :id acc-nakkaya))

   (let [words ["clojure" "java"]] 
     (reduce (fn[h v]
	       (if (some true? (map #(.contains (first v) %) words))
		 (conj h v) h)) [] keywords))

  (def country (data-feed analytics 
			   :date ["2010-01-26" "2010-02-25"]
			   :dimensions [:country]
			   :metrics [:visits]
			   :sort [:visits]
			   :num-result 100
			   :id acc-nakkaya))
  (view country)

  (def browsers (data-feed analytics 
			   :date ["2010-01-26"]
			   :dimensions [:browser]
			   :metrics [:visits]
			   :sort [:visits]
			   :num-result 10
			   :id acc-nakkaya))

  (view (bar-chart (take 4 (map first browsers))
		   (take 4 (map #(BigInteger. (last %)) browsers))
		   :title "Browser/Visits"
		   :x-label "Browsers"
		   :y-label "Visits"))

  (def view-date (data-feed analytics 
			    :date ["2009-11-26" "2010-02-25"]
			    :dimensions [:date]
			    :metrics [:visitors]
			    :sort [:visitors]
			    :num-result 10
			    :id acc-nakkaya))

  (view (line-chart (map first view-date)
		    (map #(BigInteger. (last %)) view-date)
		    :title "Visits"
		    :x-label "Date"
		    :y-label "Visits"))
  )
