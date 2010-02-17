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

I began with a function, that takes a URL,
if the URL returns a response code of 200, it will return the URL else
it will return nil.

     (defn check [url]
       (try
        (let [conn (-> (java.net.URL. url) .openConnection)]
          (.connect conn)
          (if (= 200 (.getResponseCode conn)) url))
        (catch Exception e nil)))

    user=> (check "http://nakkaya.com")
    "http://nakkaya.com"

    user=> (check "ttp://Malformed-url")
    nil

Since checking URLs does not require any coordination between threads, I
settled on using [agents](http://clojure.org/agents). Agents work just
like refs, they wrap an initial state in my case URL itself, invalid
URLs will get set to nil.

     (defn run [f]
       (let [list (line-seq (java.io.BufferedReader. (java.io.FileReader. f)))
             agents (map #(agent %) list)] 
         (doseq [agent agents] (send agent check))
         (apply await agents)
         (doseq [url (filter #(not (nil? @%)) agents)] 
           (println @url))))

run takes a file name, it will read the file and produce a sequence of
URLs to check. Each url is passed to an agent, await
will block the current thread until all jobs posted are complete. When
await returns, we filter agents that has their state set to nil and
print the rest.

#### send vs send-off

Normally for blocking actions you would want to use send-off which will
spawn it's own thread for the function, otherwise you would block the
thread pool for non blocking operations, but in this case I wanted to
block all threads so I can dump all URLs at once, and not worry about
spawning thousands of threads.

