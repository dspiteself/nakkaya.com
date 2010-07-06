---
title: Controlling Robocode Engine from Clojure
tags: clojure robocode
---

Since Robocode is designed for Java it can get really annoying when you
want to program your robots in Clojure, basically you need to keep a
REPL for compiling and testing the robot and a shell for actually
running Robocode. I was looking for a way to streamline the process,
turns out you can control Robocode engine
[programmatically](http://robocode.sourceforge.net/docs/robocode/robocode/control/package-summary.html) 
which allows us to run battles from REPL, this scheme isn't perfect but
close. 

     -Xmx512M -XX:MaxPermSize=512M

Robocode needs a lot memory to operate so depending on how you start
your REPL you need to add the above options or you will keep getting
OutOfMemoryException every 10 minutes.

    [org.nakkaya.robocode/robocode "1.7.2.0"]

I have uploaded all the required Jars to Clojars, adding the above
dependency to you project is all that is needed to get all the Jars you
need, then you can use the following snippet to start Robocode engine
from Clojure.

     (ns robocode-engine
       (:use clojure.contrib.shell-out)
       (:import (java.io File)
                (robocode.control 
                 RobocodeEngine BattlefieldSpecification BattleSpecification)
                (robocode.control.events BattleAdaptor)))
     (System/setProperty "NOSECURITY" "true")

     (defn battle-console []
       (proxy [BattleAdaptor] []
         (onBattleMessage [e] (println "Msg> " (.getMessage e)))
         (onBattleError [e] (println "Err> " (.getError e)))
         (onBattleCompleted 
          [e] 
          (println "\n\n-- Battle Complete --\n")
          (doseq [result (.getSortedResults e)]
            (println (.getTeamLeaderName result) (.getScore result))))))

     (defn engine [dir vis?]
       (let [engine (doto (RobocodeEngine. (File. dir))
                      (.setVisible vis?)
                      (.addBattleListener (battle-console)))]
         (sh "jar" "xf" "lib/clojure-1.1.0.jar" "clojure")
         (sh "mv" "clojure" "classes/")
         engine))

     (defn battle [engine rounds size robots]
       (let [[x y] size
             field (BattlefieldSpecification. x y)]
         (BattleSpecification.
          rounds field
          (into-array
           (reduce (fn[h v]
                     (conj h (first (.getLocalRepository engine v))))
                   [] robots)))))

     (defn run-battle
       ([engine rounds size robots & wait]
          (run-battle engine (battle engine rounds size robots))
          (if (not (nil? wait))
            (.waitTillBattleOver engine)))
       ([engine battle]
          (.runBattle engine battle false)))

     (defn close [engine]
       (.close engine)
       (sh "rm" "-rf" "classes/")
       (sh "mkdir" "classes"))

Now you can initialize the engine,

      (def engine (engine "/Applications/robocode/" true))

then you can compile and run your robot from REPL (assuming you robot is
in the namespace gez),

      (do 
        (compile 'gez)
        (run-battle engine 3 [800 600] 
                    ["sample.SittingDuck" 
                     "sample.Fire" 
                     "sample.Crazy"
                     "sample.RamFire" 
                     "gez*"]))
