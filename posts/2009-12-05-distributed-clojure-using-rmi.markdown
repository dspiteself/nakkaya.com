---
title: Distributed Clojure Using RMI
tags: clojure rmi
---

The [Java Remote Method Invocation
API](http://java.sun.com/javase/technologies/core/basic/rmi/index.jsp),
or RMI is the Java way of doing [remote procedure
calls](http://en.wikipedia.org/wiki/Remote_procedure_call) (RPC). Remote
Method Invocation (RMI) facilitates object function calls between [Java
Virtual Machines](http://en.wikipedia.org/wiki/Java_Virtual_Machine)
(JVMs). JVMs can be located on separate computers, one JVM can invoke
methods belonging to an object stored in another JVM. I have been
meaning to play with RMI from Clojure for a while, so I have put
together a distributed "Hello, World!" application.

A RMI application is made up of three parts,

 - [Hello.java](/code/clojure/rmi-hello/Hello.java) - Remote Interface
 - [server.clj](/code/clojure/rmi-hello/server.clj) - Server
 - [client.clj](/code/clojure/rmi-hello/client.clj) - Client


#### Remote Interface

Remote interface contains the methods that are exposed by the server
that can be called from another JVM, and it is the only part of code
that can't be written in Clojure, every method in the interface must
throw RemoteException, this can't be done with gen-interface macro. If
you create your interfaces with gen-interface you will get exceptions.

    package stub;

    import java.rmi.Remote;
    import java.rmi.RemoteException;

    public interface Hello extends Remote {
        String sayHello() throws RemoteException;
    }

Our interface exposes a single function called sayHello, when called, it
will return the "Hello, World!" string. Compile this file,

    javac Hello.java

and place it in a folder called stub, place this stub folder anywhere on
your classpath.

#### Server

First thing, server has to do is to start remote object registry,
servers and clients locate each other using this registry mechanism.

    (def rmi-registry (java.rmi.registry.LocateRegistry/createRegistry 1099))

We start the RMI registry on port 1099, if we need to programmatically
stop the registry for any reason we can use the following function,

    (defn stop-rmi []
      (java.rmi.server.UnicastRemoteObject/unexportObject rmi-registry true))

We need to create an instance of the interface we defined, containing the
implementations of the functions we exposed in the interface,

    (defn hello-server []
      (proxy [stub.Hello] [] 
        (sayHello [] "Hello, World!") ))

Server needs to be registered with the RMI registry before it can be
called from another JVM.

    (defn register-server []
      (.bind
       (java.rmi.registry.LocateRegistry/getRegistry)
       "Hello"
       (java.rmi.server.UnicastRemoteObject/exportObject (hello-server) 0)))

We bind the name "Hello" to our hello-server, now server is ready to
accept connections, we register the server and wait for incoming
connections.

    (register-server)
    (while true (Thread/sleep 1000))

#### Client

On the client side, first we locate the registry,

    (def rmi-registry (java.rmi.registry.LocateRegistry/getRegistry "127.0.0.1"))

Replace 127.0.0.1 with the IP of the server machine if the server is on
another IP. Now that we have located the registry, we can lookup objects
by name.

    (let [hello (.lookup rmi-registry "Hello")]
      (println (.sayHello hello)))

Thats all the code that is needed for a distributed "Hello, World!". To
put it all to action, run server.clj, it will start listening for
incoming connections, on the same machine or on another machine on the
same network, run client.clj, you should get "Hello, World!" returned to
the client.
