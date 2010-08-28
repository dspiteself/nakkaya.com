---
title: Applescript with Clojure
tags: applescript clojure
---

Today I was experimenting with
[AppleScript](http://en.wikipedia.org/wiki/AppleScript) engine thats
included with Java 1.6. It makes it much easier to interact with OS X
then executing osascript using Runtime.

     (let [mngr (javax.script.ScriptEngineManager.)
           engine (.getEngineByName mngr "AppleScript")] 
       (.eval engine "say \"Hello World!\""))

For simple stuff, it is as simple as asking engine manager for the
AppleScript engine and running eval on it with your script.

     (def tracks (str "tell application \"iTunes\"\n"
                      "get count of tracks of (get view of front window)\n"
                      "end tell\n"))

     (let [mngr (javax.script.ScriptEngineManager.)
           engine (.getEngineByName mngr "AppleScript")] 
       (.eval engine tracks))

    user=> 5229

When eval returns, numbers, strings, and dates are coerced into
java.lang.Double, java.lang.String, and java.util.Calendar. Collections
like lists and properties are recursively converted into
java.util.Lists, and java.util.Maps. Unknown types are usually described
as a string.

     (def play (str "on playNamedList(thisName)\n"
                    "tell application \"iTunes\"\n"
                    "play playlist thisName\n"
                    "end tell\n"
                    "end playNamedList\n"))

     (let [mngr (javax.script.ScriptEngineManager.)
           engine (.getEngineByName mngr "AppleScript")
           context (.getContext engine)
           bindings (.getBindings context javax.script.ScriptContext/ENGINE_SCOPE)]
       (.put bindings "javax_script_function", "playNamedList")
       (.put bindings javax.script.ScriptEngine/ARGV "chill")
       (.eval engine play))

In order to invoke calls that expects arguments to be set, we need to
invoke the AppleScript method using "javax\_script\_function" engine
binding. Overall it beats running osascript and parsing return values,
but it still needs more documentation.
