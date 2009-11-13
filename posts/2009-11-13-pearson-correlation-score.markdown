---
title: Pearson Correlation Score
tags: clojure programming-collective-intelligence
---

This post will cover another topic from [Programming Collective
Intelligence](http://oreilly.com/catalog/9780596529321) that is used to
define similarities between items called [Pearson
correlation
score](http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient),
the formula for this algorithm looks like the 
following, 

![Pearson Correlation Score](/images/post/pearson.gif)

This calculation returns a value between -1 and 1. Two users with a
similarity of 1 have rated every item identically. Unlike [Euclidean
Distance Score](http://nakkaya.com/2009/11/11/euclidean-distance-score/)
this formula doesn't need to be normalized. Pearson correlation score,
also accounts for average ratings for each user, a user that rates
everything 5 and a user that rates everything 1 will have a similarity
of 1. This  may or may not be the behavior you want depending on your
situation.

    (defn pearson [x y]
      (let [shrd (filter x (keys y))] 
        (if (= 0 (count shrd))
          0
          (let [sum1  (reduce (fn[s mv] (+ s (x mv))) 0 shrd)
                sum2  (reduce (fn[s mv] (+ s (y mv))) 0 shrd)
                sum1sq  (reduce (fn[s mv] (+ s (Math/pow (x mv) 2))) 0 shrd)
                sum2sq  (reduce (fn[s mv] (+ s (Math/pow (y mv) 2))) 0 shrd)
                psum (reduce (fn[s mv] (+ s (* (x mv) (y mv)))) 0 shrd)
                num (- psum (/ (* sum1 sum2) (count shrd)))
                den (Math/sqrt (* 
                                (- sum1sq (/ (Math/pow sum1 2) (count shrd)))
                                (- sum2sq (/ (Math/pow sum2 2) (count shrd)))))]
            (if (= den 0)
              0
              (double (/ num den))) ))))

Using the same critics map from  [Euclidean Distance
Score](http://nakkaya.com/2009/11/11/euclidean-distance-score/),

    user=> (pearson (critics "Lisa Rose") (critics "Gene Seymour"))
    0.39605901719066977

    user=> (pearson (critics "Lisa Rose") {})
    0
