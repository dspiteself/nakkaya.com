(ns app.tests.dummy-fs
  (:use clojure.contrib.duck-streams)
  (:use clojure.contrib.java-utils))

(defn- create-dummy-site []
  (spit 
   (file "site/dummy.markdown")
   "---
title: dummy content
description: some dummy desc
tags: unit test
---

Some dummy file for unit testing."))

(defn- destroy-dummy-site []
  (delete-file (file "site/dummy.markdown")))

(defn- create-dummy-post []
  (spit 
   (file "posts/2050-01-01-dummy-future-post.markdown")
   "---
title: dummy future post
tags: e8edaab7-25e9-45f5-8a0c 4784d643-e4e8-4673-9c0e
---

b1232b0f-58ce-4339-9272-33fb19da9a12
73c03277-9a03-4fd3-a695-7ff31cd94d92"))

(defn- destroy-dummy-post []
  (delete-file (file "posts/2050-01-01-dummy-future-post.markdown")))

(defn- create-dummy-static-folder []
  (.mkdir (file "public/dummy/")))

(defn- destroy-dummy-static-folder []
  (delete-file (file "public/dummy/")))

(defn- create-dummy-static-file []
  (spit 
   (file "public/dummy/dummy.static")
   "Hello, World!!"))

(defn- destroy-dummy-static-file []
  (delete-file (file "public/dummy/dummy.static")))

(defn create-dummy-fs []
  (create-dummy-site)
  (create-dummy-post)
  (create-dummy-static-folder)
  (create-dummy-static-file))

(defn destroy-dummy-fs []
  (destroy-dummy-site)
  (destroy-dummy-post)
  (destroy-dummy-static-file)
  (destroy-dummy-static-folder))
