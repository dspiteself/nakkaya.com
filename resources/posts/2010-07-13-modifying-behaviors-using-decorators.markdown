---
title: Modifying Behaviors Using Decorators
tags: clojure alter-ego robocode
---

My previous post on [behavior
trees](/2010/06/29/alter-ego-a-reactive-ai-library/) made use of
composite and action types, this posts serves as an example on how to
improve behaviors using decorators. They take their name from the
[software design
pattern](http://en.wikipedia.org/wiki/Decorator_pattern). In the context
of behavior trees a decorator node is a node with a single child, it
modifies the behavior of the branch in some way, i.e. you don't want
your NPC to keep kicking the door forever when it is blocked or replay an
animation without completing the cycle.

The problem with my previous Robocode tree was that every time we
execute it, it searched through the whole tree and as a consequence it
kept switching targets, using decorators we can create loops in the tree
that way we don't switch targets unless the robot we are going after
dies.

![behavior-tree-decorator](/images/post/behavior-tree-decorator.png)

*Kill All* sequence demonstrates two decorators used for looping, since 
at the beginning of the match we don't have any enemies to fight we scan
around until we find a robot to kill, until-success decorator will keep 
executing its child until it returns success meaning we found at
least one robot to fight. Next in the sequence is *Attack* branch, using
until-fail decorator it will pick a target to attack, keep attacking
until the enemy is dead then move on to a new target until select-target
fails which means all the robots in the arena are dead, having loops in
the tree allows us to execute the tree once unlike the previous example
which kept executing the tree forever in a while loop.

In order to combat robots that track our movement, two non deterministic
composite nodes exists which shuffle their children prior to execution
such as the one used in the *Strafe* branch which gives us some degree
of non determinism.


<object type="application/x-shockwave-flash" width="670" height="419" data="http://www.flickr.com/apps/video/stewart.swf?v=71377" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"> <param name="flashvars" value="intl_lang=en-us&photo_secret=ca49759dd2&photo_id=4777243989&hd_default=false"></param> <param name="movie" value="http://www.flickr.com/apps/video/stewart.swf?v=71377"></param> <param name="bgcolor" value="#000000"></param> <param name="allowFullScreen" value="true"></param><embed type="application/x-shockwave-flash" src="http://www.flickr.com/apps/video/stewart.swf?v=71377" bgcolor="#000000" allowfullscreen="true" flashvars="intl_lang=en-us&photo_secret=ca49759dd2&photo_id=4777243989&hd_default=false" height="419" width="670"></embed></object>

 - [gez.clj](/code/clojure/alter-ego-demo-robocode/gez.clj)
 - [gez.bt](/code/clojure/alter-ego-demo-robocode/gez.bt)
 - [gez.gv](/code/clojure/alter-ego-demo-robocode/gez.gv)

