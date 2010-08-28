---
title: Intercepting Links in Firefox
tags: firefox
---

While working on a Firefox extension i needed to intercept links Firefox
is about to open and stop it on certain conditions. While it seems like
an easy task it took more time then i thought due to not much
information was available online. I saw the question asked multiple
times with no definitive answer. Correct recipe turns out to be using a
observer and listen for an "http-on-examine-response".

Following script will listen all request and you will have a chance to
stop the transmission based on your rules.

    var observer = {
      observe: function(subject,topic,data){
    
        var httpChannel = 
        subject.QueryInterface(Components.interfaces.nsIHttpChannel);
        var contentType = httpChannel.getResponseHeader("Content-Type");
    
        var channel = subject.QueryInterface(Components.interfaces.nsIChannel);
        var url = channel.URI.spec;
        url = url.toString();
            
        if ( isDownloadable( url ) == true 
             &&  contentType.indexOf("html") == -1 ){

            window.getBrowser().stop();
            
            download( url );
            //alert("Wait a moment!\n"+ url );
        }
        
        //alert("Topic sent: " + topic);
      }
    };

    var observerService =
        Components.classes["@mozilla.org/observer-service;1"]
        .getService(Components.interfaces.nsIObserverService);
    observerService.addObserver(observer,"http-on-examine-response",false);

I am no extension guru, so maybe it is not the best way to do it but it
gets the job done.
