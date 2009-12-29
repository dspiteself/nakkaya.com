---
title: BEncoding Objects in Clojure
tags: clojure bencode
---

I plan on playing with the Bittorrent protocol, i already have a [bencode
decoder](http://nakkaya.com/2009/11/02/decoding-bencoded-streams-in-clojure/)
to play with torrent files, but since i need to communicate with
trackers, i need encoding. This post will walk through the steps
required to encode objects using bencoding, i have updated
[bencode.clj](/code/clojure/bencode.clj), it can now both decode and
encode.

    (defn encode [obj]
      (let [stream (ByteArrayOutputStream.)] 
        (encode-object obj stream)
        (.toByteArray stream)))

To encode an object, we call encode on it. We get a byte array
representing the encoded object, you can then write it to a file or look
at it by creating a String from it.

    (defn- encode-object [obj stream]
      (cond (string?  obj) (encode-string obj stream)
            (number? obj) (encode-number obj stream)
            (vector? obj) (encode-list obj stream)
            (map? obj) (encode-dictionary obj stream)))

encode-object is where encoding begins, depending on the type of object
passed to it, it will call the appropriate function.

    (defn- encode-string [obj stream]
      (let [bytes (.getBytes obj "UTF-8")
            bytes-length (.getBytes (str (count bytes) ":") "UTF-8")]
        (.write stream bytes-length 0 (count bytes-length))
        (.write stream bytes 0 (count bytes))))

An encoded string has the format, 

    <string length encoded in base ten ASCII>:<string data>
    4:spam -> "spam" 

so what we do is we turn the string in to a byte array, calculate it's
length write everything to stream according to the format.

    (defn- encode-number [number stream]
      (let [string (str "i" number "e")
            bytes (.getBytes string "UTF-8")]
        (.write stream bytes 0 (count bytes))))

An encoded number has the format,

    i<integer encoded in base ten ASCII>e
    i3e -> 3

we build a string by prepending "i" and appending "e" to the number
write the bytes to the stream.

    (defn- encode-list [list stream]
      (.write stream (int \l))
      (doseq [item list]
        (encode-object item stream))
      (.write stream (int \e)))

In my implementation, bencoded lists are represented as clojure vectors,
a bencoded list has the following format,

    l<bencoded values>e
    l4:spam4:eggse -> [ "spam", "eggs" ]

what we do is, iterate over the vector and for each object found, call
encode-object on it.

    (defn- encode-dictionary [dictionary stream]
      (.write stream (int \d))
      (doseq [item dictionary]
        (encode-object (first item) stream)
        (encode-object (second item) stream))
      (.write stream (int \e)))

An encoded map has the format,

    d<bencoded string><bencoded element>e 
    d3:cow3:moo4:spam4:eggse -> { "cow" => "moo", "spam" => "eggs" } 

the technique to encode a map is the same as a vector, we iterate over
the map but call encode-object twice once for the key and once for the
value.

Download [code](/code/clojure/bencode.clj).
