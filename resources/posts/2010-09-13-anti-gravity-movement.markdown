---
title: Anti-Gravity Movement
tags: clojure
---

Anti-Gravity movement (or in the field of robotics it is also known as
[potential field motion
planning](http://en.wikipedia.org/wiki/Motion_planning#Potential_Fields))
is a path finding technique, the idea is to use the formula for gravity
to calculate a path, unlike [graph search
algorithms](http://en.wikipedia.org/wiki/Graph_traversal) where we
calculate the full path between *A* and *B*, anti-gravity movement
calculates the path one step at a time. This makes it ideal for dynamic
worlds because there is no risk that a path becomes obsolete due to
changes in the world.

The theory behind anti-gravity is actually quite simple, we place
imaginary points on the map called gravity points that we want to be
repelled or attracted to, each gravity point has a force associated with
it which determines how much we want to be attracted or repelled from
the point allowing us to easily create movement patterns. At each step,
for each point we calculate gravitational force acting on us from that
point using the formula *strength/distance^n*, summing all the forces
from all the points on the map will yield the total gravitational force
acting on us then we let it push us in whatever direction it push us in.

<p id='preview'>Player</p>
<script type='text/javascript' src='/swfobject.js'></script>
<script type='text/javascript'>
	var s1 = new SWFObject('/player.swf','player','640','400','9');
	s1.addParam('allowfullscreen','true');
	s1.addParam('allowscriptaccess','always');
	s1.addParam('flashvars','file=/video/anti-gravity-movement.mp4');
	s1.write('preview');
</script>

All vector operations used are from my
[vector-2d](http://github.com/nakkaya/vector-2d) library you can grab it
from [clojars](http://clojars.org/vector-2d).

     (defn gravity-vector [u v]
       (let [force (/ 2000 (Math/pow (magnitude (- u v)) 2))
             angle (bearing u v)] 
         (vector-2d (* (Math/sin angle) force) (* (Math/cos angle) force))))

     (defn total-gravitational-force [state]
       (apply + (map #(gravity-vector (:player @state) %) (:obstacles @state))))

Using the formula *strength/distance^n*, in this case strength is 2000
and n is 2, we calculate the force then using the angle between *v* and
*u* we create a new vector from *v* to *u* which pushes us away from
*v*. Experimenting with different strength and n values will yield
different behaviours. i.e increasing n will cause you to avoid points
when you get very close to them. Doing this for all the points on the
map and then summing them up yields total gravitational force.

     (defn seek [state]
       (normalize (+ (normalize (- (:target @state) (:player @state)))
                     (total-gravitational-force state))))

     (defn steer [state]
       (when (> (dist (:player @state) (:target @state)) 1)
         (dosync (alter state assoc :player (+ (:player @state) (seek state))))))

To actually go where we want to go, we subtract our current position
from the target, normalizing this resulting vector gives us a unit
vector pointing to where we want to go, then we add total gravitational
force to the direction vector which will push us away from the obstacles
and towards our target.


Download [gravity.clj](/code/clojure/gravity.clj)
