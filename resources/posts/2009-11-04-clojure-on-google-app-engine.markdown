---
title: Clojure on Google App Engine
tags: clojure
---

I was moving some compojure applications to Google Application
Engine. After creating the same directory structure and same
configuration three times, I wanted to automate this, So I build an ant
script to automate the creation of the necessary directory
structure and source files. Download and setup SDK and compojure.

Code is hosted on [github](http://github.com/nakkaya/appengine-stub),
like any other piece of code it is released under
[Beerware](http://en.wikipedia.org/wiki/Beerware) license.

To get started, you can either download the repo or clone it. The only
thing you need to configure is your app-id and app-display-name in the
build file.

Running,

    ant setup

will download the necessary SDK and compojure files, and create source
files required for a Hello, World application.

You can test the application by running,

    ant devserver

When you are ready to deploy your application to Google, all you need to
run is,

    ant deploy

Before your first deployment you need to run appcfg.sh manually and set
your credentials by running,

    ./sdk/bin/appcfg.sh 
