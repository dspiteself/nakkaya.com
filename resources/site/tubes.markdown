---
title: Tubes Video Downloader
tags: tubes download video
description: Tubes Video Downloader
---

![tubes](images/tubes/tubes256.png "tubes") Tubes is a small utility
 that I wrote in order to save long running tech talks and presentations
 so that I can view them later when I have time. It currently supports
 Youtube, blib.tv and Vimeo.

#### Installation & Usage

##### For Mac OS X

 Download the dmg image move it to your applications folder and you are
 set.

##### For Linux & Windows

 Download the jar file. Either double click it or issue "java -jar
 tubes.jar"

In order to start your downloads just drag and drop your URLs on the
application window.

#### Requirements

Tubes is written in Clojure/Java, so as long as you have JVM, it
will work on all operating systems. It has been tested on Mac OS X
10.5, 10.6 running Java 1.5, 1.6 and Ubuntu running Java 1.6.

#### Building from source

In order to build the application from source, you need to have a
running Clojure and leiningen installation. After issuing *lein uberjar*
which builds the universal Jar, Mac OS specific parts can be build using
*lein mac-bundle* and *lein mac-dist* which builds Mac App Bundle and
.dmg files respectively.

#### Download
 - Application - 
[Mac OS
X](http://cloud.github.com/downloads/nakkaya/tubes/Tubes.dmg) - 
[Universal](http://cloud.github.com/downloads/nakkaya/tubes/tubes-standalone.jar)
 - [Source Code](http://github.com/nakkaya/tubes/tree/master) 
(Released under Beerware via GitHub)

#### Changelog
 - April 24, 2010 - Initial Release.

Application logo by
[IconFinder](http://www.iconfinder.com/icondetails/41285/128/).

Applications Icons by [Akhtar
Sheikha](http://www.iconfinder.com/icondetails/37079/48/).

For bug reports/fixes/help See [Contact](/contact.markdown)

Any feature requests are also welcome see [Contact](/contact.markdown)
