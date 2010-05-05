---
title: Consistent Hashing with Clojure
tags: clojure net-eval
---

Let's say you want to distribute some objects to a number of
nodes. The Naive approach to this problem would be to hash the object
you want to store, then using the modulus operator pick a node and
store it there,

    (mod (hash obj) n)

This scheme would work until you add or remove nodes from the cache,
now because you pick nodes from a pool of (+ n 1) or (- n 1) nodes
it would mean that all objects will hash to new nodes, defeating
the purpose of having the cache in the first place. Basically every time
you add or remove machines from the pool, you are effectively deleting
everything in the cache, so the machines that generated those objects in
the cache are swamped with requests.

[Consistent hashing](http://en.wikipedia.org/wiki/Consistent_hashing) is
a scheme that is designed to overcome this problem. Adding or removing
nodes using consistent hashing requires only
*number-of-keys/number-of-nodes* keys to be redistributed. It achieves
this by using the same hash function to both hash objects and nodes,

![consistent hashing diagram 1](/images/post/consistent-hash-1.png)

Imagine you have three objects to distribute and three nodes to store
them in, when you hash them all they will get distributed along a line,
by connecting the end points of this line we get a circle that looks
like the one above, to figure out where to store an object we move
clockwise around the circle and pick the node whose hash is bigger
than the hash of the object we want to store, meaning Object 1 will be
stored in Node B, Object 2 in Node C and so on. So each node holds
objects with hashes lower than itself but greater than the
preceding node.

![consistent hashing diagram 2](/images/post/consistent-hash-2.png)

But what happens when we remove a node, above diagram shows the cache
after Node C is removed. When we apply the same logic Object 1 still
belongs to Node B and Object 3 still belongs to Node A only Object 2's
mapping has changed it now belongs to Node A. This is how consistent
hashing allows us to add or remove nodes without destroying whole
cache, only a small portion of the cache is shuffled around.

     (defn sha1 [obj]
       (let [bytes (.getBytes (with-out-str (pr obj)))] 
         (apply vector (.digest (MessageDigest/getInstance "SHA1") bytes))))

We begin our implementation by creating a function to calculate hashes
for our objects, you can use any hash function you like even roll your
own, only requirement is that it has to mix values well so values like
*foobar1* and *foobar2* don't end up on the same node. I went with
SHA-1 but MD5 is just as good.

     (defn hash-node [node replicas]
       (map #(hash-map (sha1 (str node %)) node) (range replicas)))

Since we are placing nodes on the ring using their hashes, it is still
possible to have non uniform distribution of keys, to combat this every
time we place a node on the ring we create some number of virtual nodes,
these virtual nodes still map to the same pyshsical node but allows us
to have a more uniform distribution of keys on the hash ring.

     (defn add-node [circle node replicas]
       (apply merge circle (hash-node node replicas)))

     (defn remove-node [circle node replicas]
       (apply dissoc circle (map first (map keys (hash-node node replicas)))))

     (defn consistent-hash [nodes replicas]
       (reduce (fn[h v] (add-node h v replicas)) (sorted-map) nodes))

Creating the consistent hash is pretty straight forward, for every node
we want to add, we add *node\*replicas* number of points to the map, when
we want to remove a node we just reverse the process.

     (defn tail-map [circle hash]
       (filter #(<= 0 (compare (key %) hash)) circle))

     (defn lookup [circle obj]
       (if-not (empty? circle)
         (let [hash (sha1 obj)
               tail-map (tail-map circle hash)] 
           (if (empty? tail-map)
             (val (first circle))
             (val (first tail-map))))))

In order to locate a node to place our object in, first we calculate the
objects hash, than using this hash, we create a view of the sorted-map
that has all the keys equal or bigger than the hash we want to place,

    (tail-map (sorted-map [3 4] :b [1 2] :a [6 7] :c [6 8] :d) [3 3])
    ([[3 4] :b] [[6 7] :c] [[6 8] :d])
    (tail-map (sorted-map [3 4] :b [1 2] :a [6 7] :c [6 8] :d) [20 20])
    ()

Assuming our hash is *[3 3]* tail map will return the list of nodes
whose hashes are bigger then the objects hash so first item in the list
will be the node that will hold our value, in the case where we get an
empty sequence it means we gone around our hash ring and the object
belongs to the first node on the ring.

This is all the code that is needed to implement consistent hashing, now
we can implement our own distributed hash table using the
[net-eval](http://nakkaya.com/net-eval.markdown) library,

     (deftask init-cache []
       (def consistent-cache (ref {})))

     (deftask get-from-cache [key]
       (@consistent-cache key))

     (deftask add-to-cache[key val]
       (dosync (alter consistent-cache merge {key val})))

First task will allow us to initialize a cache on the remote nodes,
second and third tasks will allow us to query and add to the cache
respectively.

     (defn remote-init [nodes]
       (doseq [[ip port] nodes]
         (net-eval [[ip port #'init-cache]])))

     (defn remote-get [hash nodes key]
       (let [[ip port] (lookup hash key)] 
         (deref (first (net-eval [[ip port #'get-from-cache key]])))))

     (defn remote-add [hash nodes key val]
       (let [[ip port] (lookup hash key)]
         (net-eval [[ip port #'add-to-cache key val]])))

Now all thats left to do is send those tasks for execution to the remote
nodes,

       (def nodes [["127.0.0.1" 9999]
                   ["10.211.55.3" 9999]])

       (def cons-hash (consistent-hash nodes 3))

       (lookup cons-hash  "some_key")
       (lookup cons-hash  "some_other_key")

       (remote-init nodes)
       (remote-add cons-hash nodes "some_key" "42")
       (remote-get cons-hash nodes "some_key")

Download [Code](/code/clojure/consistent-hash.clj).
