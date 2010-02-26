---
title: Writing Leiningen Plugins 101
tags: clojure leiningen
---

I'm trying to switch, building my projects using
[Ant](http://ant.apache.org/) to
[leiningen](http://github.com/technomancy/leiningen). Almost all of them
requires customs tasks such as building native executables, move files
around etc. Which requires I have to come up with a lein plugin for each
ant task, unfortunately not much documentation exists about writing
lein plugins, this post collects bits and pieces of information I
gathered over the web.

To begin with lein tasks are functions named "your-task" defined in the
namespace "leiningen.your-task". They take a project argument containing
information defined in defproject and command-line arguments. For simple
tasks or quickly testing something, you can simply define them in
project.clj after the defproject definition,

     (ns leiningen.foo)
     (defn foo [project & args] (println "Hello Foo!!"))

Now lein should have a new task named foo, running it should print
"Hello Foo!!". Of course for longer tasks, you are not going to want it
cluttering your project.clj, they can be placed under leiningen/ folder
**not** src/leiningen/. Since tasks are just functions, making a task
depend on another task is as easy as calling depencies on top of the
function.

     ;;leiningen/bar.clj
     (ns leiningen.bar)

     (defn bar [projects & args] 
       (leiningen.foo/foo projects args)
       (println "Hello Bar!!"))

Now running bar task should give you, "Hello Foo!!" and "Hello
Bar!!". For sharing plugins across projects create a separate lein
project for the plugin, after creating a Jar with "lein jar" you have two
options, you can either push it to clojars and add your plugin as a
dev-dependency for your project or just move the jar to the lib folder
of the project, either way lein will pick it up.
