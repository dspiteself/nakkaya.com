---
title: Clojure Persistence for Java Programmers
tags: clojure
---

Using Java for a long time, when i needed to save some data structure to
disk, my first response was to [serialize](http://www.j2ee.me/developer/technicalArticles/Programming/serialization/)
it to a file. While working on a Clojure application, i did just that, it
worked half the time because not every data structure implements
[Serializable](http://java.sun.com/j2se/1.4.2/docs/api/java/io/Serializable.html).

Then i remembered Clojure being a lisp, code is data. This allows you to
dump everything as a String to a file and read it back as a data
structure.

    user=> (doc prn)
    -------------------------
    clojure.core/prn
    ([& more])
      Same as pr followed by (newline). Observes *flush-on-newline*
    nil

You can pass prn a vector,map or any object you want, it will print the
object to the output stream.

    (defstruct db :file :data)
 
    (defn write-db [db]
      (binding [*out* (java.io.FileWriter. (:file db))]
        (prn (:data db))))

By binding \*out\* to a FileWriter we can easily dump any object to a
file,

    (write-db (struct db "test" [1 2 3]))
    (write-db (struct db "test" {:test "test" :ax "ax"}))

To read it back we use read-string function,

    user=> (doc read-string)
    -------------------------
    clojure.core/read-string
    ([s])
      Reads one object from the string s
    nil

read-string takes a string and returns an object,

    (defn read-db [fname]
      (try
       (let [object (read-string (slurp fname))]
        (struct db fname object))
       (catch Exception e nil)))

    (read-db "test")

