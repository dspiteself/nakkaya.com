---
title: Pretty Printing XML with Clojure
tags: clojure compojure xml
---

The other day, I did some XML cleanup. I am posting the snippet here for
safekeeping purposes in case I need to refer to it later.

     (defn ppxml [xml]
       (let [in (javax.xml.transform.stream.StreamSource.
                 (java.io.StringReader. xml))
             writer (java.io.StringWriter.)
             out (javax.xml.transform.stream.StreamResult. writer)
             transformer (.newTransformer 
                          (javax.xml.transform.TransformerFactory/newInstance))]
         (.setOutputProperty transformer 
                             javax.xml.transform.OutputKeys/INDENT "yes")
         (.setOutputProperty transformer 
                             "{http://xml.apache.org/xslt}indent-amount" "2")
         (.setOutputProperty transformer 
                             javax.xml.transform.OutputKeys/METHOD "xml")
         (.transform transformer in out)
         (-> out .getWriter .toString)))

Now you can pass your XML string,

    (ppxml "<root><child>aaa</child><child/></root>")

and get the pretty printed version,

     <?xml version="1.0" encoding="UTF-8"?>
     <root>
       <child>aaa</child>
       <child/>
     </root>

You can also use it to pretty print Compojure output either manually,

     (ppxml (html
             [:html
              [:head
               [:title "Hello World"]]
              [:body "Hello World!"]]))

or using a middleware,

     (defn with-ppxml [handler]
       (fn [request]
         (let [response (handler request)]
           (assoc response :body (ppxml (:body response))))))

and have your pretty printed HTML,

     <html> 
       <head> 
         <title>Hello World</title> 
       </head> 
       <body>Hello World!</body> 
     </html> 
