---
title: Zipping XML with Clojure
tags: clojure xml
---

I've written about [parsing XML with
Clojure](/2009/10/10/processing-xml-with-clojure/) before, the problem
is, as I wanted more from the document, loops started to get uglier. I
thought it was time to try the
[zip-filter](http://richhickey.github.com/clojure-contrib/zip-filter-api.html)
API, it provides [XPath](http://en.wikipedia.org/wiki/XPath) style
navigation in Clojure. Not much documentation about it exists but as
usual [source
code](http://github.com/richhickey/clojure-contrib/blob/81b9e71effbaf6aa2945cd684802d87c762cdcdd/src/clojure/contrib/zip_filter/xml.clj#L57)
was very helpful.

We begin by including the required libraries,

    (ns zp
      (:require [clojure.zip :as zip]
                [clojure.xml :as xml])
      (:use clojure.contrib.zip-filter.xml))

And a utility function to convert a XML string into a zip structure,

    (defn zip-str [s]
      (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

I was playing with the XML example snippet from Wikipedia, which it
claims has all the XML concepts in it.

    (def paintings (zip-str "<?xml version='1.0' encoding='UTF-8'?>
    <painting>
      <img src='madonna.jpg' alt='Foligno Madonna, by Raphael'/>
      <caption>This is Raphael's 'Foligno' Madonna, painted in
      <date>1511</date>-<date>1512</date>.</caption>
    </painting>"))


xml-> call provides a way to navigate through the elements and
attributes in an XML document. If we want to access the caption tag we
can use,

    (xml-> paintings :caption text)

Or to get the dates it was painted in,

    (xml-> paintings :caption :date text)

For accessing attributes, there is the attr function which returns the
matching XML attribute,

    (xml-> paintings :img (attr :src))

These calls return a sequence, you can either call first on them or use
the xml1-> function, which calls it for you. zip-filter can do more than
these simple examples, these are what I needed at the moment for more
examples, have a look at the bottom of the [source
code](http://github.com/richhickey/clojure-contrib/blob/81b9e71effbaf6aa2945cd684802d87c762cdcdd/src/clojure/contrib/zip_filter/xml.clj#L57).
