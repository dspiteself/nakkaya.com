---
title: jMonkeyEngine Hello World in Clojure
tags: clojure
---

I was experimenting with jMonkeyEngine in order to visualize a
simulation, later I scrapped the idea thinking it was going to take way
to much time just to make it look cool and switched to a 2D
engine. I had already ported their Hello World example to Clojure so I
am posting it here to give anyone who want to play with jMonkeyEngine a
head start, I have uploaded all necessary jars to Clojars, use lein to
grab everything you need,

     (defproject monkey-test "1.0.0-SNAPSHOT"
       :description "FIXME: write"
       :dependencies [[org.clojure/clojure "1.1.0"]
                      [org.clojure/clojure-contrib "1.1.0"]
                      [org.clojars.nakkaya.jmonkeyengine/jme "2.0.1"]]
       :native-dependencies [[penumbra/lwjgl "2.4.2"]]
       :dev-dependencies    [[native-deps "1.0.0"]])

All tutorials on jMonkeyEngine wiki, makes use of SimpleGame class, we
add stuff to our scene by attaching objects to rootNode of SimpleGame
which is defined as protected hence we can't use this in Clojure, we
don't have access to protected fields when using proxy so below example
uses StandartGame instead,

     (ns monkey-test.core
       (:import (com.jmex.game StandardGame)
                (com.jmex.editors.swing.settings GameSettingsPanel)
                (com.jmex.game.state DebugGameState)
                (com.jmex.game.state GameStateManager)
                (com.jme.scene.shape Box)
                (com.jme.math Vector3f)
                (com.jme.bounding BoundingSphere)))

     (defn standart-game []
       (let [game (StandardGame. "A Simple Test")]
         (GameSettingsPanel/prompt (.getSettings game))
         (.start game)))

     (defn add-box [state]
       (let [box (Box. "Mybox" (Vector3f. 0 0 0) (Vector3f. 1 1 1))] 
         (.setModelBound box (BoundingSphere. ))
         (.updateModelBound box)
         (.updateRenderState box)
         (.attachChild (.getRootNode state) box)
         (.updateRenderState (.getRootNode state))))

     (defn -main []
       (let [game (standart-game)
             state (DebugGameState.)]
         (add-box state)
         (.attachChild (GameStateManager/getInstance) state)
         (.setActive state true)))
