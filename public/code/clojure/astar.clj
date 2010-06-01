(ns astar.core
  (:use clojure.test)
  (:use [clojure.contrib.seq-utils :only [find-first indexed includes?]]))

(defn manhattan-distance [[x1 y1] [x2 y2]]
  (+ (Math/abs (- x2 x1)) (Math/abs (- y2 y1))))

(defn node 
  ([x y] {:x x :y y})
  ([x y f g h] {:x x :y y :f f :g g :h h}))

(defn update-cost [{:keys [x y]} start end]
  (let [g (manhattan-distance start [x y])
	h (manhattan-distance [x y] end)
	f (+ g h)] 
    (node x y f g h)))

(defn eq? [{:keys [x y]} that]
  (and (= x (that :x)) (= y (that :y))))

(defn grid [map open closed start end]
  {:map map :open open :closed closed :start start :end end})

(defn closed? [{:keys [closed]} node]
  (some true? (map #(eq? % node) closed)))

(defn open? [{:keys [open]} node]
  (some true? (map #(eq? % node) open)))

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

(defn has-next? [{:keys [open]}]
  (> (count open) 0))

(defn done? [{:keys [open end]}]
  (let [{x :x y :y} (first open)]
    (= [x y] end)))

(defn head [{:keys [open]}]
  (first open))

(defn remove-head [{:keys [map open closed start end]}]
  (grid map (rest open) (conj closed (first open)) start end))

(defn get-node [{:keys [open]} node]
  (find-first #(eq? % node) open))

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

(defn draw-map [area start end]
  (let [path (time (search area start end))]
    (doseq [[r row] (indexed area)]
      (let [col (indexed row)] 
	(println 
	 (map #(let [[c v] %] 
		 (cond (includes? path [c r]) \X
		       (= 1 v) \#
		       :default " ")) col))))))

(comment
  (def maze1 [[0 0 0 0 0 0 0]
	      [0 0 0 1 0 0 0]
	      [0 0 0 1 0 0 0]
	      [0 0 0 1 0 0 0]
	      [0 0 0 0 0 0 0]])

  (draw-map maze1 [1 2] [5 2])


  (def maze2 [[0 0 0 0 0 0 0]
	      [0 0 1 1 1 0 0]
	      [0 0 0 1 0 0 0]
	      [0 0 0 1 0 0 0]
	      [0 0 0 1 0 0 0]])

  (draw-map maze2 [1 3] [5 2])

  (def maze3 [[0 1 0 0 0 1 0]
	      [0 1 0 1 0 1 0]
	      [0 1 0 1 0 1 0]
	      [0 1 0 1 0 1 0]
	      [0 0 0 1 0 0 0]])

  (draw-map maze3 [0 0] [6 0])

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

  (deftest test-node
    (let [smallest (first (sort-by :f [(node 2 2 4 5 6) (node 2 2 1 2 3)]))] 
      (is (= true (eq? (node 2 2) (node 2 2))))
      (is (= true (eq? (node 2 2 1 2 3) (node 2 2))))
      (is (= true (eq? (node 2 2) smallest)))
      (is (< 0 (:f (update-cost (node 2 2) [1 2] [5 5]))))))

  (deftest test-grid
    (let [node1 (update-cost (node 2 2) [1 2] [5 5])
	  node2 (update-cost (node 5 5) [1 2] [5 5])
	  normal-grid (grid maze2 [node1 node2] [] [1 3] [2 3])
	  done-grid (grid maze2 [node1] [node2] [1 3] [2 2])
	  ;;
	  node3 (update-cost (node 2 2) [1 2] [5 5])
	  node4 (update-cost (node 1 1) [1 2] [5 5])
	  edge-grid (grid maze2 [node3] [node4] [1 3] [2 2])
	  node3-edges (edges edge-grid node3)]
      (is (= true (closed? done-grid node2)))
      (is (= true (open? done-grid node1)))
      (is (= true (some false? (map #(eq? % node4) node3-edges))))
      (is (= true (eq? node1 (head normal-grid))))
      (is (= true (eq? node2 (head (remove-head normal-grid)))))
      (is (= false (has-next? (remove-head (remove-head grid)))))
      (is (= true (done? done-grid)))))

  (deftest test-add
    (let [start [1 1] end [5 5]
	  node1 (update-cost (node 2 2) start end)
	  node2 (update-cost (node 2 3) start end)
	  grid (grid maze1 [node1] [] start end)]
      ;;add fresh
      (is (open? (add-node grid node1 node2) node2))
      (is (eq? node1 (get-node grid node1)))))

  )
