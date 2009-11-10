---
title: Using Java Mail API from Clojure
tags: clojure
---

Java API does not provide a way to send mail or interface with POP/IMAP
servers, but Sun does provide a framework to build mail and messaging
applications. 

You can get the JavaMail API
[here](http://java.sun.com/products/javamail/). Following is a simple
function that allows you to send mail from clojure.

 - activation.jar
 - mailapi.jar
 - smtp.jar

If you just need to send email from your clojure applications, just grab
the jar's listed above. I use this snippet to send email through GMail,
i have not tested it anywhere else, but it should work.

    (defn mail [& m]
      (let [mail (apply hash-map m)
            props (java.util.Properties.)]

        (doto props
          (.put "mail.smtp.host" (:host mail))
          (.put "mail.smtp.port" (:port mail))
          (.put "mail.smtp.user" (:user mail))
          (.put "mail.smtp.socketFactory.port"  (:port mail))
          (.put "mail.smtp.auth" "true"))

        (if (= (:ssl mail) true)
          (doto props
            (.put "mail.smtp.starttls.enable" "true")
            (.put "mail.smtp.socketFactory.class" 
                  "javax.net.ssl.SSLSocketFactory")
            (.put "mail.smtp.socketFactory.fallback" "false")))

        (let [authenticator (proxy [javax.mail.Authenticator] [] 
                              (getPasswordAuthentication 
                               []
                               (javax.mail.PasswordAuthentication. 
                                (:user mail) (:password mail))))
              session (javax.mail.Session/getDefaultInstance props authenticator)
              msg     (javax.mail.internet.MimeMessage. session)] 
      
          (.setFrom msg (javax.mail.internet.InternetAddress. (:user mail)))
          (doseq [to (:to mail)] 
            (.setRecipients msg 
                            (javax.mail.Message$RecipientType/TO)
                            (javax.mail.internet.InternetAddress/parse to)))
          (.setSubject msg (:subject mail))
          (.setText msg (:text mail))
          (javax.mail.Transport/send msg))))

    (mail :user user@gmail.com"
          :password "pass"
          :host "smtp.gmail.com"
          :port 465
          :ssl true
          :to ["nurullah@nakkaya.com" ]
          :subject "I Have Rebooted." 
          :text "I Have Rebooted.")
