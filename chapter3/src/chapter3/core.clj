(ns chapter3.core)

(def whole-numbers (iterate inc 1))

(def cycles (cycle (take 5 whole-numbers)))
(def cycles-finitized (take 20 cycles))

(def interleaved (interleave whole-numbers ["A" "B" "C"]))

(def interposed (interpose "," ["apples" "bananas" "grapes"]))

;; List comprehension
(defn quick-sort [[pivot & coll]]
  (when pivot
    (let [lower (for [x coll :when (< x pivot)] x)
          upper (for [x coll :when (> x pivot)] x)
          pivot-coll (conj (empty coll) pivot)]
      (lazy-cat (quick-sort lower) pivot-coll (quick-sort upper)))))
