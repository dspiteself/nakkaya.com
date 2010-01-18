(ns app.tests
  (:use compojure)
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.java-utils)
  (:use clojure.test)
  (:use :reload-all app.routes)
  (:use :reload-all app.util))

(defn create-dummy-site []
  (spit 
   (file "site/dummy.markdown")
   "---
title: dummy content
description: some dummy desc
tags: unit test
---

Some dummy file for unit testing."))

(defn destroy-dummy-site []
  (delete-file (file "site/dummy.markdown")))

(defn create-dummy-post []
  (spit 
   (file "posts/2050-01-01-dummy-future-post.markdown")
   "---
title: dummy future post
tags: e8edaab7-25e9-45f5-8a0c 4784d643-e4e8-4673-9c0e
---

b1232b0f-58ce-4339-9272-33fb19da9a12
73c03277-9a03-4fd3-a695-7ff31cd94d92"))

(defn destroy-dummy-post []
  (delete-file (file "posts/2050-01-01-dummy-future-post.markdown")))

(defn request-resource [resource web-app]
  (let [request  {:request-method :get, :uri resource}]
    (web-app request)))

(deftest test-markdown
  (is (= "unit test"  
	 (:tags (:metadata (read-markdown "site/dummy.markdown")))))
  (is (= "some dummy desc"  
	 (:description (:metadata (read-markdown "site/dummy.markdown")))))
  (is (= "dummy content"
	 (:title (:metadata (read-markdown "site/dummy.markdown")))))
  (is (= "Some dummy file for unit testing."
	 (re-find #"Some dummy file for unit testing."
		  (:content (read-markdown "site/dummy.markdown"))))))

(deftest test-route-site
  (is (= 200
	 (:status (request-resource "/dummy.markdown" web-app))))
  (is (= {"Content-Type" "text/html"}
	 (:headers (request-resource "/dummy.markdown" web-app))))
  (is (= "<title>dummy content</title>"
	 (re-find #"<title>dummy content</title>"
		  (:body (request-resource "/dummy.markdown" web-app)))))
  (is (= "<meta content=\"some dummy desc\" name=\"description\" />"
	 (re-find #"<meta content=\"some dummy desc\" name=\"description\" />"
		  (:body (request-resource "/dummy.markdown" web-app)))))
  (is (= "<meta content=\"unit test\" name=\"keywords\" />"
	 (re-find #"<meta content=\"unit test\" name=\"keywords\" />"
		  (:body (request-resource "/dummy.markdown" web-app)))))
  (is (= "Some dummy file for unit testing"
	 (re-find #"Some dummy file for unit testing"
		  (:body (request-resource "/dummy.markdown" web-app))))))


(deftest test-routes
  ;;check unique tags
  (is (= "e8edaab7-25e9-45f5-8a0c"
	 (re-find #"e8edaab7-25e9-45f5-8a0c"
		  (:body (request-resource "/tags/" web-app)))))
  (is (= "4784d643-e4e8-4673-9c0e"
	 (re-find #"4784d643-e4e8-4673-9c0e"
		  (:body (request-resource "/tags/" web-app)))))
  ;;/latest-posts/:page/
  (is (= "b1232b0f-58ce-4339-9272-33fb19da9a12"
	 (re-find #"b1232b0f-58ce-4339-9272-33fb19da9a12"
		  (:body (request-resource "/latest-posts/0/" web-app)))))
  (is (= "73c03277-9a03-4fd3-a695-7ff31cd94d92"
	 (re-find #"73c03277-9a03-4fd3-a695-7ff31cd94d92"
		  (:body (request-resource "/latest-posts/0/" web-app)))))
  ;;/archives/
  (is (= "January 2050"
	 (re-find #"January 2050"
		  (:body (request-resource "/archives/" web-app)))))
  ;;/:year/:month/
  (is (= "b1232b0f-58ce-4339-9272-33fb19da9a12"
	 (re-find #"b1232b0f-58ce-4339-9272-33fb19da9a12"
		  (:body (request-resource "/2050/01/" web-app)))))
  (is (= "73c03277-9a03-4fd3-a695-7ff31cd94d92"
	 (re-find #"73c03277-9a03-4fd3-a695-7ff31cd94d92"
		  (:body (request-resource "/2050/01/" web-app)))))
  ;;/:year/:month/:day/:title/
  (is (= "b1232b0f-58ce-4339-9272-33fb19da9a12"
	 (re-find #"b1232b0f-58ce-4339-9272-33fb19da9a12"
		  (:body (request-resource "/2050/01/01/dummy-future-post/" web-app)))))
  (is (= "73c03277-9a03-4fd3-a695-7ff31cd94d92"
	 (re-find #"73c03277-9a03-4fd3-a695-7ff31cd94d92"
		  (:body (request-resource "/2050/01/01/dummy-future-post/" web-app)))))
  ;;/rss-feed
  (is (= "b1232b0f-58ce-4339-9272-33fb19da9a12"
	 (re-find #"b1232b0f-58ce-4339-9272-33fb19da9a12"
		  (:body (request-resource "/rss-feed" web-app)))))
  (is (= "73c03277-9a03-4fd3-a695-7ff31cd94d92"
	 (re-find #"73c03277-9a03-4fd3-a695-7ff31cd94d92"
		  (:body (request-resource "/rss-feed" web-app)))))
  ;;/
  (is (= "b1232b0f-58ce-4339-9272-33fb19da9a12"
	 (re-find #"b1232b0f-58ce-4339-9272-33fb19da9a12"
		  (:body (request-resource "/" web-app)))))
  (is (= "73c03277-9a03-4fd3-a695-7ff31cd94d92"
	 (re-find #"73c03277-9a03-4fd3-a695-7ff31cd94d92"
		  (:body (request-resource "/" web-app)))))
  ;;redirect
  (is (= 301
	 (:status (request-resource "/2050/01/01/dummy-future-post" web-app))))
  (is (= {"Location" "/2050/01/01/dummy-future-post/"}
	 (:headers (request-resource "/2050/01/01/dummy-future-post" web-app))))
  (is (= 404
	 (:status (request-resource "/20500101dummy" web-app)))))

(create-dummy-site)
(create-dummy-post)
(run-tests)
(destroy-dummy-site)
(destroy-dummy-post)
