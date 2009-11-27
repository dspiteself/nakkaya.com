---
title: Clojure Concurrency - 404 Checker
tags: clojure
---

Recently a friend of mine wanted me to scrape some URLs from
[DMOZ](http://www.dmoz.org/). Unfortunately some URLs in DMOZ pages
no longer exists. This is my third time scraping URLs from DMOZ so
I already have a Java class to filter URLs based on the their HTTP
response code.

My Java code is single threaded, since it was late at night when I wrote
it, I didn't mind it taking couple of hours to finish since I was going
to bed anyway. Concurrency being Clojure's biggest strength, I wanted a
concurrent version in Clojure.

I began with a function, that takes a vector of URLs and a URL to check,
if the URL returns a response code of 200, it will add the URL to the
given vector of URLs, else it will just return the vector given.

    (defn check [list url]
      (try
       (let [conn (-> (java.net.URL. url) .openConnection)]
         (.connect conn)
         (if (= 200 (.getResponseCode conn))
           (conj list url) list))
       (catch Exception e list)))

    user=> (check [] "http://nakkaya.com")
    ["http://nakkaya.com"]

    user=> (check ["http://nakkaya.com"] "http://nakkaya.com/doesNotExist")
    ["http://nakkaya.com"]

    user=> (check [] "ttp://Malformed-url")
    []

Since checking URLs does not require any coordination between threads, I
settled on using [agents](http://clojure.org/agents). Agents work just
like refs, they wrap a initial state in my case and empty vector, which
will hold the resulting valid URL list.

    (defn run [f]
      (let [list (line-seq (java.io.BufferedReader. (java.io.FileReader. f)))
            result (agent [])] 
        (doseq [url list] (send result check url))
        (await result)
        (shutdown-agents)
        (doseq [res @result]  (println res)))) 

run takes a file name, it will read the file and produce a sequence of
URLs to check. Each url is passed to the agent using send call, await
will block the current thread until all jobs posted are complete. Next
we shutdown the thread pool agents use. When shutdown is complete agent
will contain a vector of valid URLs that returned 200 as their response
code. All thats left is iterate through the agent and print the list of
valid URLs.

#### send vs send-off

Normally for blocking actions you would want to use send-off which will
spawn it's own thread for the function, otherwise you would block the
thread pool for non blocking operations, but in this case I wanted to
block all threads so I can dump all URLs at once, and not worry about
spawning thousands of threads.

