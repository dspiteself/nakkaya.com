---
title: alter-ego - A Reactive AI Library
description: alter-ego is a reactive AI library based on the concept of behavior trees.
tags: clojure alter-ego behavior-tree
---

alter-ego is a reactive AI library for Clojure based on the concept of
behavior trees. A behavior tree is a technique for organizing
collections of states and the decision processes for when to move
between them. Behavior trees have many similarities to FSMs but unlike
FSMs it is very easy to see logic, they are fast to execute and easy to
maintain, which makes them suitable for representing complex and
potentially parallel behaviors.

#### Source

Project is hosted at github, grab it
[here](http://github.com/nakkaya/alter-ego).

alter-ego is also available via [clojars](http://clojars.org/alter-ego).

    [alter-ego "0.0.2-SNAPSHOT"]

> Due to a glitch in lein you need run jar/uberjar command twice in order
> to build the project.

#### Usage

After compiling alter-ego the resulting uberjar is set to run the
bundled behavior tree editor. Editor allows you to build manage behavior
trees. Output of the editor is a Clojure vector which can than be loaded
by the library or Clojure itself if you want to do some preprocessing.

#### Examples

 - [Overview of the Library and a Sample Robocode Agent](/2010/06/29/alter-ego-a-reactive-ai-library/)
 - [Modifying Behaviors Using Decorators](/2010/07/13/modifying-behaviors-using-decorators/)

#### Further Reading

 - [Handling Complexity in the Halo 2 AI](http://www.gamasutra.com/gdc2005/features/20050311/isla_01.shtml) 
 - [My Liner Notes for Spore/Spore Behavior Tree Docs](http://chrishecker.com/My_Liner_Notes_for_Spore/Spore_Behavior_Tree_Docs)
 - [Behavior Trees for Next-Gen AI](http://aigamedev.com/insider/presentations/behavior-trees/#recording)

#### License

Eclipse Public License 1.0
([http://opensource.org/licenses/eclipse-1.0.php](http://opensource.org/licenses/eclipse-1.0.php))
