---
title: net-eval - Simple distributed computing.
description: Simple and powerful distributed computing.
tags: clojure net-eval distributed-computing
---

net-eval allows you to evaluate expressions in parallel on remote
network nodes. net-eval handles all the work necessary to communicate to
other nodes, distribute expressions for evaluation and collect results.

#### Souce

Project is hosted at github, grab it
[here](http://github.com/nakkaya/net-eval).

#### Installation

On the machine which you want to distribute work from, add the src/ folder to
your classpath, on the remote nodes, you need to have a REPL server
running, if you build the project with lein resulting Jar will fire up a
REPL server.

#### Usage

In order to evaluate expressions remotely you need to define a
task,

     (deftask atask [] 
       (+ 1 2))

You can call and test tasks just like functions, when you are ready to
distribute the work call net-eval,

     (net-eval [["192.168.1.1" 9999 #'atask]
                ["192.168.1.2" 9999 #'atask]
                ["192.168.1.3" 9999 #'atask]
                ["192.168.1.4" 9999 #'atask]])

net-eval takes a sequence of vectors, containing host port and task to
send and returns a sequence future objects, each corresponding to a
result from a remote node. For tasks that takes arguments you can append
any number of arguments after the task,

     (deftask atask [a] 
       (range a))

     (net-eval [["192.168.1.1" 9999 #'atask 5]
                ["192.168.1.2" 9999 #'atask 5]
                ["192.168.1.3" 9999 #'atask 5]
                ["192.168.1.4" 9999 #'atask 5]])

#### License

Beerware Revision 42
