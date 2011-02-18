---
title: Vision - OpenCV wrapper for Clojure
description: Vision is a OpenCV wrapper for Clojure.
tags: clojure opencv
flatter: yes
---

Vision is a OpenCV wrapper for Clojure.

#### Installation

There are no pre build packages available. For now you have to build it
your self. Only dependency is the OpenCV library.

Run,

    cmake .

in the *resources/lib* directory to create your platform specific build
file then build the shared library, for Linux/OS X run,

    make

On the Clojure side you need to start the JVM with the
jna.library.path pointing to the location of the shared library, for a
leiningen project it can be done by adding the following to your
project.clj,

    :jvm-opts ["-Djna.library.path=/pat/to/lib/"]

In addition, you must have *vision.jar* and *jna.jar* on your classpath.

#### Usage

resources/examples/ folder contains examples to give a feel for the
API, I also have the following hacks using Vision,

 - [Lane Detection using Clojure and OpenCV](/2011/01/24/lane-detection-using-clojure-and-opencv/)

#### Source

Project is hosted at github, grab it
[here](http://github.com/nakkaya/vision).

#### License

Beerware Revision 42
