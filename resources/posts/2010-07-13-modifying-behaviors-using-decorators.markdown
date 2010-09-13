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

<p id='preview'>Player</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','670','419','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/alter-ego-robocode-2.mp4');
	s1.write('preview');
</script>

 - [gez.clj](/code/clojure/alter-ego-demo-robocode/gez.clj)
 - [gez.bt](/code/clojure/alter-ego-demo-robocode/gez.bt)
 - [gez.gv](/code/clojure/alter-ego-demo-robocode/gez.gv)

