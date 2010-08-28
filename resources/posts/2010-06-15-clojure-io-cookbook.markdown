---
title: Clojure I/O Cookbook
tags: clojure
---

There are a number of ways for us to read a file. If the file is small
enough and can be held in memory, simplest approach is to use slurp
which will return a string containing the content of the file,

    (slurp "some.txt")

For files that you can't or don't want to hold in memory, we can use
BufferedReader, line-seq combo and process files on a line by line basis,

     (with-open [rdr (java.io.BufferedReader. 
                      (java.io.FileReader. "project.clj"))]
       (let [seq (line-seq rdr)]
         (count seq)))

These days it is more common (at least for me) to retrieve a URL then it
is to read file, 

     (defn fetch-url[address]
       (with-open [stream (.openStream (java.net.URL. address))]
         (let  [buf (java.io.BufferedReader. 
                     (java.io.InputStreamReader. stream))]
           (apply str (line-seq buf)))))

     (fetch-url "http://google.com")

Above will work on text files but corrupt binary files because
BufferedReader assumes it is dealing with textual data, for downloading
a binary file (video, music etc.) and saving it to a file on disk,

     (defn fetch-data [url]
       (let  [con    (-> url java.net.URL. .openConnection)
              fields (reduce (fn [h v] 
                               (assoc h (.getKey v) (into [] (.getValue v))))
                             {} (.getHeaderFields con))
              size   (first (fields "Content-Length"))
              in     (java.io.BufferedInputStream. (.getInputStream con))
              out    (java.io.BufferedOutputStream. 
                      (java.io.FileOutputStream. "out.file"))
              buffer (make-array Byte/TYPE 1024)]
         (loop [g (.read in buffer)
                r 0]
           (if-not (= g -1)
             (do
               (println r "/" size)
               (.write out buffer 0 g)
               (recur (.read in buffer) (+ r g)))))))

     (fetch-data "http://google.com")


Or if you prefer interacting with the socket directly,

     (defn socket [host port]
       (let [socket (java.net.Socket. host port)
             in (java.io.BufferedReader. 
                 (java.io.InputStreamReader. (.getInputStream socket)))
             out (java.io.PrintWriter. (.getOutputStream socket))]
         {:in in :out out}))

     (def conn (socket "irc.freenode.net" 6667))
     (println (.readLine (:in conn)))

Now for writing stuff back to disk,

     (require 'clojure.contrib.duck-streams)
     (clojure.contrib.duck-streams/spit "output.txt" "test")

If you don't want to depend on the contrib library you can bind \*out\* to
a FileWriter and print the content,

     (binding [*out* (java.io.FileWriter. "some.dat")]
       (prn {:a :b :c :d}))

