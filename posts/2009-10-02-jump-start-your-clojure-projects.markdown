---
title: Jump Start Your Clojure Projects
tags: clojure ant
---

Since i started playing with clojure, i have been jumping from one toy
project to another. This required creating the same code structure,
copying same jars all over again and modifying the same ant file for the
new project.

After fourth or fifth time, i got bored and created a clojure project
stub. By setting a few variables inside the build.xml file you can have
a working Hello, World application contained in a executable jar.

#### Setup

To get started either clone my
[project](http://github.com/nakkaya/clojure-stub) or
[download](http://github.com/nakkaya/clojure-stub/zipball/master)
it. Modify build.xml file and set your project properties such as
project name and application jar name.

Running,

    ant setup

will download the necessary jar files and will place them under extLibs/
directory. Create the src/ directory and populate it.

All Class-Path settings are handled in the build file so you can start
coding right away.

By default required jars are downloaded from my repository, but you can
change it and set it to point anywhere you like.

#### Targets

- run     - will setup required class path's and run the application
- prepare - will unzip necessary jar's to create a single executable jar.
- compile - will build the application and create single executable jar.
- clean   - will clean up the build folder and created jar.
- deps    - can redownload needed jar's.
- setup   - is ran once to create all files required to create a "Hello,
World" application with your settings.
