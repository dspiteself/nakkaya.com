---
title: alter-ego - A Reactive AI Library
tags: alter-ego clojure robocode
---

[alter-ego](/alter-ego.markdown) is a reactive AI library based on the
concept of behavior trees. Behavior trees combines a number of AI
techniques such as Hierarchical State Machines, Scheduling, Planning,
and Action Execution. Their strength comes from the fact that it is very
easy to see logic, they are fast to execute and easy to maintain.

Behavior trees allow programmers to piece together reusable blocks of
code which can be as simple as looking up a variable in game state to
running an animation, then the behavior tree is used to control the flow
and execution of these blocks of code.

As its name implies a behavior tree is a tree structure, made up of
three types of nodes, action, decorator, composite. Composite and
decorator nodes are used to control the flow within the three and action
nodes are where we execute code they return success or failure and their
return value is then used to decide where to navigate next in the tree.

In alter-ego actions are functions, if you are using the built in editor
for composing trees an action is function that takes a single variable
called blackboard this is what the behavior tree uses to communicate
with the outside world, if you are programmatically building your tree, you
can pass arbitrary number of variables. Actions are used to change state such
as calculating a new path or shooting at the player.

Selector and sequence nodes are workhorse internal nodes. Selector node
will try to execute its first child, if it returns success it will also
return success if it fails it will try executing its next child until
one of its children returns success or it runs out of children at which
point it will return failure. This property allows us to choose which
behavior to run next,

![An Example Selector Node](/images/post/selector.png)

On the other hand a sequence represents a series of behaviors that we
need to accomplish. A sequence will try to execute all its children from
left to right, if all of its children succeeds sequence will also
succeed, if one of its children fails sequence will stop and return
failure.

![An Example Sequence Node](/images/post/sequence.png)

With the above tree, root selector first checks attack sequence,
attack in return executes action *Player In Range?* if it succeeds we
pick a weapon, face the player and fire. If *Player In Range?* fails we
stop execution and it causes attack sequence to fail and selector tries
to execute Taunt sequence.

Thats enough theory to put together something that works, 

![Simple Robocode AI](/images/post/robocode.png)

This represents a very simple AI logic for a robocode bot, every time we
execute the tree, we scan for others bots in the arena, pick one to
attack, then we execute Move selector, if we are not too close we
move forward, if we are too close forward sequence will fail and we fall
back to  back action. We want our robot to be always ready to fire so
the first thing we do in Fire sequence is to turn the turret towards the
target then we check if we are within gun range, if we are we fire, if
we are not we bail out which takes us back to scan, whether we fire or
not we always keep the gun locked on the target.

On the Clojure side, we only need to implement actions (green leaf
nodes), rest is handled in the bundled behavior tree editor this is what
makes behavior trees powerful, you can reorganize, cut, copy, paste
nodes without changing a single line of code,

![Behavior Tree Editor](/images/post/editor.png)

     (defn fire [blackboard]
       (from-blackboard blackboard [robot]
                        (.fire robot 3) true))

Actions are Clojure functions, they take one variable blackboard which
is a reference holding a map. Blackboard is what nodes use to
communicate with each other such as when an action picks a target it is
written to the blackboard then another action reads that value and acts
on it. When working with Java methods you need to keep in mind that
return value of the action will be coerced to boolean that is what
determines success or failure of the action when methods return null
even when they succeed such as fire above you need to return true
explicitly. *from-blackboard* is a convenience macro that will lookup
the values of its bindings in the blackboard,

    (from-blackboard blackboard [robot])

will expand to,

    (let [robot (:robot @blackboard)])

After we define all our actions, all we need to do is load the behavior
tree and execute it,

     (defn -run [robot]
       (setup robot)
       (let [tree (load-tree "/Users/nakkaya/Desktop/robocode/tank.bt" 
                             (.blackboard robot))]
         (forever (exec tree))))

Note that when loading the tree you can provide your own blackboard or
one will be automatically provided for your nodes even if you don't plan
on using it.

<object type="application/x-shockwave-flash" width="650" height="406" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=1b4182492a&photo_id=4732500548&hd_default=false"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=1b4182492a&photo_id=4732500548&hd_default=false" height="406" width="650"></embed></object>

Files,

 - [alter-ego](/alter-ego.markdown)
 - [build.xml](/code/clojure/alter-ego-demo-robocode/build.xml)
 - [tank.clj](/code/clojure/alter-ego-demo-robocode/tank.clj)
 - [tank.bt](/code/clojure/alter-ego-demo-robocode/tank.bt)
