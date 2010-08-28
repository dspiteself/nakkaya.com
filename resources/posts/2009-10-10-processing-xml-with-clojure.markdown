---
title: Processing XML With Clojure
tags: clojure xml
---

Although XML is nice in theory, i have always hated dealing with it. It
requires so much boiler plate code just to parse or create a simple XML
file. Recently i needed to do some XML processing and i still can't
believe how easy it was to create and parse XML in clojure.


#### Creating XML

Clojure contrib includes a library for creating XML called
prxml. Vectors become XML tags. Such as,

    (prxml [:p {:class "greet"} [:i "Ladies & gentlemen"]])
    ; => <p class="greet"><i>Ladies &amp; gentlemen</i></p>

First let's define some data to turn in to XML.

    (def data #{{:title "Clojure" :link "http://clojure.org"  
                 :description "Clojure Homepage"}

                {:title "Java"    :link "http://java.sun.com" 
                 :description "JVM Homepage"}

                {:title "Debian"  :link "http://debian.org"   
                 :description "Debian Homepage"}})


By default prxml function outputs to the screen if you want to output to
a string use prxml in combination with with-out-str.

    (defn articles []
      (reduce 
       (fn [feed v]
         (conj feed 
               [:item 
                [:title (:title v)] 
                [:url (:url v)] 
                [:description (:description v)]]))
       () data))

We build a list of vectors for every node in the XML.

    [:item 
      [:title "Clojure"] 
      [:url "clojure.org"] 
      [:description "Clojure Homepage"]]

    [:item 
      [:title "Java"] 
      [:url "java.sun.com"] 
      [:description "JVM Homepage"]]

If you wrap everything, it takes less than 20 lines of code to produce
an RSS feed.

    (defn xml-data []
      (with-out-str
       (prxml [:decl! {:version "1.0"}] 
              [:rss {:version "2.0"} 
               [:channel 
                [:title "The Site"]
                [:link "http://site.com"]
                [:description "The Site"]
                (articles)]])))

#### Parsing XML

Parsing XML is even easier, clojure core has built in support for XML
processing. clojure.xml/parse can take a File, InputStream or String
naming a URI and return a tree of the xml/element struct-map. You can
then treat it like any other sequence.

Such as to iterate over all the titles in the XML file,

    (let [input-stream (ByteArrayInputStream. (.getBytes (xml-data) "UTF-8"))]
      (for [x (xml-seq (parse input-stream))
            :when (= :title (:tag x))]
        (:content x)))

    rss=> (["site-title"] ["Clojure"] ["Java"] ["Debian"])
