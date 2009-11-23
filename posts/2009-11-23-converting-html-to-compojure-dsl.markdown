---
title: Converting HTML to Compojure DSL
tags: clojure compojure
---

Compojure DSL for creating HTML/XML is great unless you have a lot of
HTML code already written. At first my plan was to parse it, write it to
a file and manually format it, then I stumbled on
[this](http://groups.google.com/group/compojure/browse_thread/thread/9909b205f08c151d#)
post from compojure mailing list, it is a small utility function written
by Robin Brandt. It converts the given HTML file to clojure/compojure
DSL.

    (ns de.evernet2000.util
      (:use clojure.contrib.str-utils)
      (:use clojure.contrib.duck-streams)
      (:use clojure.contrib.pprint)
      (:use [clojure.xml :only (parse)])
      (:import (java.io File)))

    (defn format-attrs
      [m]
      (when m
        (format "%s" m)))

    (defn empty-when-null
      [x]
      (if (nil? x)
        ""
        x))

    (declare format-full-node)

    (defn format-node
      [node]
      (cond
        (string? node) (format "\"%s\"" (.trim node))
        (nil? node) nil
        :else (format-full-node node)))

    (defn format-full-node
      [node]
      (format "[%s %s %s]\n"
              (:tag node)
              (empty-when-null (format-attrs (:attrs node)))
              (str-join " " (map format-node (:content node)))))

    (defn transform-file
      [filename]
      (print (pprint (read-string (format-node (parse filename))))))

It will complain if you have badly written HTML, in my case it only
complained about a bunch of br statements, a simple search and replaced
fixed it. If you can't get it to accept your HTML try running it
through [JTidy](http://jtidy.sourceforge.net/), that should fix it.
