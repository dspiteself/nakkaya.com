---
title: Path Finding Using A-Star in Clojure
tags: clojure
---

For a recent project, I had to implement A* (A-Star) in Clojure, since
it's a very popular path finding algorithm used in gaming I thought it
might be interesting to other clojurians too. 

AStar uses best-first search to find the the least-cost path from a
given initial node to one goal node (out of one or more possible
goals). Functions,

 - *g(x)* - cost of getting to that node from starting node.
 - *h(x)* - cost of getting to the goal node from current node.
 - *f(x)* - *g(x)+h(x)*

are used to determine the order in which search visits nodes. Beginning
with the start node, we keep track of two lists, open and closed, open
list contains the list of nodes to traverse sorted by their *f(x)* cost,
closed list contains the list of nodes that we have processed. At each
step algorithm removes the first node on the open list, calculate
*f(x)*, *g(x)* and *h(x)* values for its neighbors and add the ones that
are not on the closed list to the open list. This is done until goal
node has been found or no nodes are left on the open list.

In a nutshell we will,

 - Add the starting node to the open list.
 - Loop
   - Remove the node with the lowest *f(x)* from the open list.
   - Add it to closed list.
   - Calculate 8 adjacent squares.
   - Filter neighbors that are not on the closed list and walkable.
   - For each square
     - If it is not on the open list, calculate F, G and H costs,  make
       the current square parent of this square and add it open list.
     - If it is on the open list, check to see if this path to that
       square is better using the G cost, a lower G indicates a better
       path if so change its parent to this square and recalculate F G and H
       costs.
 - Until
   - Target node is added to the closed list indicating a path
     has been found.
   - No more nodes left in the open list indicating there is no path
     between nodes.

Before diving into code, let me start with a disclaimer, my first
priority was clarity so this is not the most efficient
implementation, for my use case it can find the path around 20-30
milliseconds which is good enough for me but there are tons of stuff you
can do to improve performance if you need to.

       (def maze1 [[0 0 0 0 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 0 0 0 0]])

Surface is represented using a 2D vector of 0s and 1s, 0 denoting
walkable nodes and 1 denoting non walkable nodes.

     (defn node 
       ([x y] {:x x :y y})
       ([x y f g h] {:x x :y y :f f :g g :h h}))

Each node is represented by a map, which holds the information about
its position and F, G, H costs associated with it.

     (defn manhattan-distance [[x1 y1] [x2 y2]]
       (+ (Math/abs (- x2 x1)) (Math/abs (- y2 y1))))

     (defn update-cost [{:keys [x y]} start end]
       (let [g (manhattan-distance start [x y])
             h (manhattan-distance [x y] end)
             f (+ g h)] 
         (node x y f g h)))

Quality of the path found will depend on the distance function used to
calculate F, G, and H costs, for this implementation I choose to use
[Manhattan distance](http://en.wikipedia.org/wiki/Taxicab_geometry)
since it is cheaper to calculate then [Euclidean
distance](http://en.wikipedia.org/wiki/Euclidean_distance) but keep in
mind that different distance metrics will produce different paths so
depending on your condition expensive metrics can produce more natural
looking paths.

     (defn search 
       ([map start end]
          (let [[x y] start
                open [(update-cost (node x y) start end)]
                closed []]
            (search (grid map open closed start end))))
       ([grid]
          (if (has-next? grid)
            (if-not (done? grid)
              (let [node (head grid)
                    grid (remove-head grid)]
                (recur (add-nodes grid node (edges grid node))))
              (path (head grid) (remove-head grid))))))

Search function is where it all happens and it pretty much summarizes
all of the above steps. First we create two vectors that will hold our
open and closed lists then add the starting node to open list, and call
search, we will keep calling search until no elements are left on the
open list or first node on the open list is our goal node. Unless we are
done we remove the first item on the open list, put it to closed list
and process nodes around it.

     (defn edges [grid n]
       (let [{map :map} grid
             {x :x y :y} n
             edges (for [tx (range (- x 1) (+ x 2)) 
                         ty (range (- y 1) (+ y 2))] (node tx ty))
             max-x (dec (count (first map)))
             max-y (dec (count map))]
         (filter #(let [{nx :x ny :y} %]
                    (cond (or (< nx 0) (< ny 0)) false
                          (or (> nx max-x) (> ny max-y)) false
                          (= [x y] [nx ny]) false
                          (= (nth (nth map ny) nx) 1) false
                          (closed? grid %) false
                          :default true)) edges)))

For each node we are examining, we need to build a list of nodes around
it. We filter them by checking if the node contains a 1 in its place on
the map which means we can't go over it or it is already in the closed
list which means we have already looked at it.

     (defn add-node [g p n]
       (let [{open :open start :start end :end} g
             {parent-x :x parent-y :y} p]
         (if-not (open? g n)
           (let [n (assoc (update-cost n start end) :p {:x parent-x :y parent-y})]
             (assoc g :open (conj open n)))
           (let [pn (get-node g n)
                 open (filter #(not (eq? n %)) open)
                 n (assoc (update-cost n start end) :p {:x parent-x :y parent-y})]
             (if (< (:g n) (:g pn))
               (assoc g :open (conj open n)) g)))))

     (defn add-nodes [grid parent nodes]
       (let [grid (reduce (fn[h v] (add-node h parent v)) grid nodes)]
         (assoc grid :open (sort-by :f (:open grid)))))

After we get the list of adjacent nodes, they need to be added to the open
list for further exploration, for nodes that are not on the open list,
we calculate their costs and append them to the open vector, for nodes
that are already on the open list, we check which one, the one on the
open list or the one we just calculated has a lower G cost if the new
one has a lower G cost we replace the one on the list with the new
one. After we add all the nodes, we resort the open list using F cost.

     (defn parent [g n]
       (let [{closed :closed} g
             {{x :x y :y} :p} n]
         (find-first #(eq? % (node x y)) closed)))

     (defn path [end grid]
       (let [nodes (loop [path [end]
                          p (parent grid end)]
                     (if (nil? p)
                       path
                       (recur (conj path p) (parent grid p))))]
         (reverse (map #(vector (% :x) (% :y)) nodes))))

When we hit our target node, we need to work backwards starting from
target node, go from each node to its parent until we reach the starting
node. That is our path.

       (def maze1 [[0 0 0 0 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 0 0 0 0]])

       (draw-map maze1 [1 2] [5 2])

     astar.core=> "Elapsed time: 10.938 msecs"
     (      X      )
     (    X # X    )
     (  X   #   X  )
     (      #      )
     (             )

       (def maze2 [[0 0 0 0 0 0 0]
                   [0 0 1 1 1 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]
                   [0 0 0 1 0 0 0]])

       (draw-map maze2 [1 3] [5 2])

     astar.core=> "Elapsed time: 10.162 msecs"
     (    X X X    )
     (  X # # # X  )
     (    X #   X  )
     (  X   #      )
     (      #      )

       (def maze3 [[0 1 0 0 0 1 0]
                   [0 1 0 1 0 1 0]
                   [0 1 0 1 0 1 0]
                   [0 1 0 1 0 1 0]
                   [0 0 0 1 0 0 0]])

       (draw-map maze3 [0 0] [6 0])

     astar.core=> "Elapsed time: 8.98 msecs"
     (X #   X   # X)
     (X # X # X # X)
     (X # X # X # X)
     (X # X # X # X)
     (  X   #   X  )

       (def maze4 [[0 0 0 0 0 0 0 0]
                   [1 1 1 1 1 1 1 0]
                   [0 0 0 1 0 0 0 0]
                   [0 0 0 1 0 0 0 0]
                   [0 0 0 1 0 0 0 0]
                   [0 0 0 1 1 1 0 1]
                   [0 0 0 0 0 1 0 1]
                   [0 0 0 0 0 1 0 1]
                   [0 0 0 0 0 0 0 1]
                   [1 1 1 1 0 1 1 1]
                   [0 0 0 1 0 0 0 0]
                   [0 0 0 1 0 0 0 0]
                   [0 0 0 0 0 0 0 0]])

       (draw-map maze4 [0 0] [0 12])

     astar.core=> "Elapsed time: 20.136 msecs"
     (X X X X X X X  )
     (# # # # # # # X)
     (      #     X  )
     (      #   X    )
     (      #   X    )
     (      # # # X #)
     (          # X #)
     (          # X #)
     (          X   #)
     (# # # # X # # #)
     (      # X      )
     (      # X      )
     (X X X X        )

Download [code](/code/clojure/astar.clj).

#### References
 - [A* search algorithm](http://en.wikipedia.org/wiki/A*_search_algorithm)
 - [A* Pathfinding for Beginners](http://www.policyalmanac.org/games/aStarTutorial.htm)
