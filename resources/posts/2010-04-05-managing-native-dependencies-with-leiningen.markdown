---
title: Managing Native Dependencies with Leiningen
tags: clojure leiningen
---

This post will cover the steps required to package, deploy and use native
dependencies with [leiningen](http://github.com/technomancy/leiningen)
using David Nolen's
[native-deps](http://github.com/swannodette/native-deps) plugin.

First we need to package native libs according to the native-deps spec,
for that create two folders named, "native/" and "lib/". Jar files will
go into the lib/ folder, native libs (.so,.jnilib,.dll) will go into
the native/ folder. Folder structure I used to package RxTx libs for Mac
OS X is,

     /lib
     /lib/RXTXcomm.jar

     /native/
     native/macosx/x86/librxtxSerial.jnilib

Next step is to Jar them up,

    jar -cMf rxtx-macosx-native-deps-2.1.7.jar native lib

Before we push to clojars we need to create a POM file for it,

     <?xml version="1.0" encoding="UTF-8"?>
     <project>
       <modelVersion>2.1.7</modelVersion>
       <groupId>org.clojars.nakkaya</groupId>
       <artifactId>rxtx-macosx-native-deps</artifactId>
       <version>2.1.7</version>
       <name>RxTx</name>
     </project>

Then push to clojars with,

    scp pom.xml rxtx-macosx-native-deps-2.1.7.jar clojars@clojars.org:

Now native dependencies are ready for deployment, assuming we want to
create new clodiuno project which depends on RxTx, we create a new
leiningen project adding RxTx as a native dependency,

     (defproject ardu-test "1.0.0-SNAPSHOT"
       :description "FIXME: write"
       :dependencies [[org.clojure/clojure "1.1.0"]
                      [org.clojure/clojure-contrib "1.1.0"]
                      [clodiuno "0.0.1-SNAPSHOT"]]
       :native-dependencies [[org.clojars.nakkaya/rxtx-macosx-native-deps "2.1.7"]]
       :dev-dependencies    [[native-deps "1.0.0"]])

Running "lein deps" followed by "lein native-deps" will unpack everthing
into their respective folders, jars will go into the lib folder, library
files will go into the native folder. If you use Slime thats all you
need to do, running "lein swank" will take care of library path, but for
inferior-lisp or running scripts in this directory we need to add the
native folder to our library path so that java picks up the native libs
by adding the following property to the java command,

    -Djava.library.path=./native/macosx/x86/
