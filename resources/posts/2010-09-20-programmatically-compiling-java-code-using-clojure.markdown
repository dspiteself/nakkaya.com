---
title: Programmatically Compiling Java Code Using Clojure
tags: clojure java
---

Everytime I am using some cryptic Java code as reference I get the
feeling that my life would be so much easier if I could just copy/paste
parts of it into REPL and see what that variable called *a* is doing, so
I spend some time playing with [Java Compiler
API](http://download.oracle.com/javase/6/docs/api/javax/tools/package-summary.html)
hoping it would save me some time later. Below snippet allows you to
compile and execute Java code from a string,

     (defn javac [src]
       (let [name (gensym)
             kind javax.tools.JavaFileObject$Kind/SOURCE
             extension (.extension kind)
             uri (java.net.URI.  (str "string:///dummy/" name extension))
             class-path ["-classpath" (System/getProperty "java.class.path")]
             src (proxy [javax.tools.SimpleJavaFileObject] [uri kind]
                    (getCharContent 
                     [_] 
                     (str "package dummy;" "public class " name " {" src "}")))]
         (-> (javax.tools.ToolProvider/getSystemJavaCompiler)
             (.getTask nil nil nil class-path nil [src]) 
             .call)
         (let [fc (java.io.File. (str name ".class"))
               fd (java.io.File. "./dummy")] 
           (if (not (.exists fd)) 
             (.mkdir fd))
           (.renameTo fc (java.io.File. fd (str fc))))
         name))

This scheme isn't perfect, compiled class will be written to the current
working directory instead of the structure dictated by its package so it
needs to be moved manually and once a class is loaded/used recompiling
it won't reflect your changes so above method uses *gensym* to create a
unique class each time you compile under the package *dummy*,

     (javac
       (str
        "  public static void main(String args[]) {"
        "    System.out.println(\"This is in another java file\");"
        "  }"))

     user=> (dummy.G__7/main (into-array String [""]))
     user=> This is in another java file
     nil

     (javac
       (str
        "  public int two() {"
        "    return 1+1;"
        "  }"))

     user=> (.two (dummy.G__12.))
     user=> 2
