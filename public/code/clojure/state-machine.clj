(ns state-machine.core
  (:use [clojure.contrib.seq-utils :only [find-first flatten]]))

(defn state-machine [transition-table initial-state]
  (ref initial-state :meta transition-table))

(defn- switch-state? [conds]
  (if (empty? conds)
    true
    (not (some false? (reduce #(conj %1 (if (fn? %2) (%2) %2)) [] conds)))))

(defn- first-valid-transition [ts]
  (find-first #(= (second %) true)
	      (map #(let [{conds :conditions 
			   transition :transition
			   on-success :on-success} %]
		      [transition (switch-state? conds) on-success]) ts)))

(defn update-state [state]
  (let [transition-list ((meta state) @state)
	[transition _ on-success] (first-valid-transition transition-list)]
    (if-not (nil? transition)
      (do 
	(if-not (nil? on-success)
	  (on-success))
	(dosync (ref-set state transition))))))

(defmacro until-state [s c & body] 
  `(while (not= (deref ~s) ~c) 
	  ~@body
	  (update-state ~s)))



(def traffic-light
     {:green [{:conditions [] :transition :yellow}]
      :yellow  [{:conditions [] :transition :red}]
      :red [{:conditions [] :transition :green}]})

(let [sm (state-machine traffic-light :green)] 
  (dotimes [_ 4]
    (println @sm)
    (update-state sm)))

;;
;;
;;

(defn pop-char [char-seq]
  (dosync (ref-set char-seq (rest @char-seq))))

(defn find-lisp [char-seq]
  (let [start-trans {:conditions []
		     :on-success #(pop-char char-seq)
		     :transition :start}
	found-l-trans {:conditions [#(= (first @char-seq) \l)] 
		       :on-success #(pop-char char-seq)
		       :transition :found-l}]

    {:start [found-l-trans
	     start-trans]

     :found-l [found-l-trans
	       {:conditions [#(= (first @char-seq) \i)] 
		:on-success #(pop-char char-seq)
		:transition :found-i}
	       start-trans]

     :found-i [found-l-trans
	       {:conditions [#(= (first @char-seq) \s)] 
		:on-success #(pop-char char-seq)
		:transition :found-s}
	       start-trans]

     :found-s [found-l-trans
	       {:conditions [#(= (first @char-seq) \p)] 
		:on-success #(do (println "Found Lisp")
				 (pop-char char-seq))
		:transition :start}
	       start-trans]}))

(let [char-seq (ref "ablislasllllispsslis")
      sm (state-machine (find-lisp char-seq) :start)] 
  (dotimes [_ (count @char-seq)]
    (update-state sm)))

(comment
  (defn prepare-nodes [state]
    (let [table (meta state)]
      (partition
       2 (flatten 
	  (map (fn [s]
		 (let [[name transitions] s
		       transitions (flatten (map :transition transitions))]
		   (map #(vector name %) transitions))) table)))))
  (use 'vijual)
  (do (println )
      (draw-graph (prepare-nodes (state-machine traffic-light :start))))
  )
