(ns lsystem
  (:use clojure.contrib.seq-utils)
  (:use turtle))

(def koch-curve
     {:variables #{:F}
      :constants #{:+ :-}
      :start [:F]
      :rules {:F [:F :+ :F :- :F :- :F :+ :F]}
      :actions {:F forward :+ left :- right}
      :angle 90
      :step 10})

(def sierpinski-triangle
     {:variables #{:A :B}
      :constants #{:+ :-}
      :start [:A]
      :rules {:A [:B :- :A :- :B]
	      :B [:A :+ :B :+ :A]}
      :actions {:A forward :B forward :+ left :- right}
      :angle 60
      :step 10})

(def dragon-curve
     {:variables #{:X :Y}
      :constants #{:F :+ :-}
      :start [:F :X]
      :rules {:X [:X :+ :Y :F]
	      :Y [:F :X :- :Y]}
      :actions {:F forward :+ left :- right}
      :angle 90
      :step 10})

(def pentigree
     {:variables #{:F}
      :constants #{:+ :-}
      :start [:F :- :F :- :F :- :F :- :F]
      :rules {:F [:F :- :F :+ :+ :F :+ :F :- :F :- :F]}
      :actions {:F forward :+ left :- right}
      :angle 72
      :step 10})

(defn variable? [grammer symbol]
  (contains? (:variables grammer) symbol))

(defn apply-rules [grammer sentence]
  (flatten
   (map #(if (variable? grammer %) ((:rules grammer) %) %) sentence)))

(defn l-system [grammer n]
  (loop[acc n sentence (:start grammer)]
    (if (= 0 acc)
      sentence (recur (dec acc) (apply-rules grammer sentence)))))

(defn draw-system [turtle grammer sentence]
  (doseq [letter sentence]
    (let [action (letter (:actions grammer))] 
      (cond
       (or (= action forward) (= action back)) 
       (action turtle (:step grammer))
       (or (= action left) (= action right)) 
       (action turtle (:angle grammer))))))

(comment

  (defn setup-turtle [turtle x y]
    (pen-up turtle)
    (right turtle 90)
    (go turtle x y)
    (pen-down turtle))

  (doto (turtle 820 450)
    (setup-turtle -410 0)
    (draw-system koch-curve (l-system koch-curve 4))
    (show))

  (doto (turtle 700 600)
    (setup-turtle -300 -200)
    (draw-system sierpinski-triangle (l-system sierpinski-triangle 6))
    (show))

  (doto (turtle 450 600)
    (setup-turtle -50 -200)
    (draw-system dragon-curve (l-system dragon-curve 10))
    (show))

  (doto (turtle 400 400)
    (setup-turtle -90 -100)
    (draw-system pentigree (l-system pentigree 3))
    (show))

  )
