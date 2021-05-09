(ns chapter4.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn lazy-seq-fib
  ([]
   (concat [0 1] (lazy-seq-fib 0N 1N)))
  ([a b]
   (let [n (+ a b)]
     (lazy-seq (cons n (lazy-seq-fib b n))))))

;; Defined as a function to prevent holding the head and therefore not letting
;; the sequence be collected
(defn lazy-seq-fib-composed []
  (map first (iterate (fn [[a b]]
                        [b (+ a b)])
                      [0N 1N])))

;; Coin toss laziness examples
(defn count-heads-pairs [coll]
  (loop [cnt 0 coll coll]
    (if (empty? coll)
      cnt
      (let [next-two-h (= :h (first coll) (second coll))
            next-cnt (if next-two-h
                       (inc cnt)
                       cnt)]
        (recur next-cnt (rest coll))))))

;; So this is ok but its sorta hard to read, and obscures what we *really* want
;; to do which is consider everything with it's immediate neighbor, so why not
;; generate a sequence that is the data we actually want, a seq of neighbors and
;; count the ones that are [:h :h]
(defn by-pairs [coll]
  ;; Helper fn that will return the next pair or nil
  (let [take-pair #(when (next %) (take 2 %))]
    ;; Generate a lazy seq.
    (lazy-seq
     ;; until there are no more pairs
     (when-let [pair (seq (take-pair coll))]
       ;; return the pair followed by the rest of the pairs of the seq.
       (cons pair (by-pairs (rest coll)))))))
(defn count-heads-pairs-2 [coll]
  (count (filter
          #(= (seq [:h :h]) %)
          (by-pairs coll))))

;; That is better but its still manually generating a lazy-seq, I could use
;; sequence library to do that for me.  I'm partitioning this into a sliding
;; window of elements...partition does that!
(defn count-heads-pairs-3 [coll]
  (count (filter #(= (seq [:h :h]) %)
                 (partition 2 1 coll))))

;; Even better, declaritive and clearer!  But we can improve, the count ->
;; filter composition is a little smelly.  Why not use composition directly and functionally?
(def count-if ^{:doc "Count items matching a filter"} (comp count filter))

(defn count-heads-pairs-4 [coll]
  (let [pairs (partition 2 1 coll)]
    (count-if #(= (seq [:h :h]) %) pairs)))

;; There is still some noise and lack of reuse, I can make even more first class functions:
(defn count-runs
  "Count runs matching a predicate"
  [n pred coll]
  (count-if #(every? pred %) (partition n 1 coll)))

(def count-heads-pairs-5 (partial count-runs 2 #(= % :h)))
;; Now with this I can change run length or even what constitutes a match very easily
(def count-heads-triples (partial count-runs 3 #(= % :h)))

;; Recursion revisited
(declare my-odd? my-even?)

(defn my-odd? [n]
  (if (= n 0)
    false
    (my-even? (dec n))))

(defn my-even? [n]
  (if (= n 0)
    true
    (my-odd? (dec n))))

;; This can be fixed by combining concepts, eg parity in this case:
(defn parity [n]
  (loop [n n par 0]
    (if (= n 0)
      par
      (recur (dec n) (- 1 par)))))
(defn my-odd-parity? [n] (= 1 (parity n)))
(defn my-even-parity? [n] (= 0 (parity n)))

;; Or a trampoline, which either returns a function to call next, or a value
;; when it is complete.  This stops stack growth, but at the cost of extra returns/calls
;; Don't do this but here's an example:
(defn trampoline-fibo [n]
  (let [fib (fn fib [f-2 f-1 current]
              (let [f (+ f-2 f-1)]
                (if (= n current)
                  f
                  #(fib f-1 f (inc current)))))]
    (cond
      (= n 0) 0
      (= n 1) 1
      :else (fib 0N 1 2))))
;; So long as this returns a new function, trampoline will recur for us.
(trampoline trampoline-fibo 9)

;; Here's how my-odd and even would look:
(declare my-even-tramp? my-odd-tramp?)
(defn my-odd-tramp? [n]
  (if (= n 0)
    false
    #(my-even-tramp? (dec n))))

(defn my-even-tramp? [n]
  (if (= n 0)
    true
    #(my-odd-tramp? (dec n))))

;; Or replace recursion with laziness

;; Replace symbol in nested lists is mutually recursive and could cause issues.
(declare replace-symbol replace-symbol-expression)
(defn replace-symbol [coll oldsym newsym]
  (if (empty? coll)
    ()
    (cons (replace-symbol-expression (first coll) oldsym newsym)
          (replace-symbol (rest coll) oldsym newsym))))
(defn replace-symbol-expression [symbol-expr oldsym newsym]
  (if (symbol? symbol-expr)
    (if (= oldsym symbol-expr)
      newsym
      symbol-expr)
    (replace-symbol symbol-expr oldsym newsym)))

;; Let's show this
(defn deeply-nested [n]
  (loop [n n result '(bottom)]
    (if (= n 0)
      result
      (recur (dec n) (list result)))))
;; To break teh recursion, all we need to do is use lazy-seq
(defn- coll-or-scalar [x & _] (if (coll? x) :collection :scalar))
(defmulti replace-symbol-2 coll-or-scalar)
(defmethod replace-symbol-2 :collection [coll oldsym newsym]
  (lazy-seq
   (when (seq coll)
     (cons (replace-symbol-2 (first coll) oldsym newsym)
           (replace-symbol-2 (rest coll) oldsym newsym)))))
(defmethod replace-symbol-2 :scalar [obj oldsym newsym]
  (if (= obj oldsym) newsym obj))

;; How laziness can hurt, like reading a file, and what to do:
(defn non-blank? [s]
  (not (str/blank? s)))
(defn non-blank-lines-seq [file-name]
  (let [reader (io/reader file-name)]
    (filter non-blank? (line-seq reader))))
;; No bueno. Sure, it's lazy, but the reader is never closed because of that, if we did this:
(defn non-blank-lines [file-name]
  (with-open [reader (io/reader file-name)]
    (into [] (filter non-blank?) (line-seq reader))))
;; This is ok, but we could OOM on large files since now we're not lazy...☹.
(defn non-blank-lines-eduction [reader]
  (eduction (filter non-blank?) (line-seq reader)))
;; educiton is a "suspended transformation" but processes entire input every time.
(defn line-count [file-name]
  (with-open [reader (io/reader file-name)]
    (reduce (fn [cnt _] (inc cnt)) 0 (non-blank-lines-eduction reader))))
