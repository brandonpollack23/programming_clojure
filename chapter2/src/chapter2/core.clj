(ns chapter2.core
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(defn numbers []
  (+ 2 3)
  (+ 1 2 3 4))

(defn ratios []
  (/ 22 7))

(defn quotiant-division
  "Integer division yo"
  []
  (quot 22 7))

(defn remainders
  "Integer division but the remainder yo. This is different from mod because it
  preserves the negative"
  []
  (rem -22 7))

(defn arbitrary-preciscion []
  (+ 1 (/ 0.00001M 10000000000000000000000000000M)))

;; Collections

(defn there-are-vectors []
  [1 2 3])

(defn there-are-lists []
  '(1 2 3))

(defn there-are-sets []
  (let [the-set #{1 2 3}
        bigger-set (conj the-set 4)
        biggest-set (set/union bigger-set #{5 6})]
    biggest-set))

(defn there-are-maps []
  (let [the-map {:a 1 :b 2 :c 3}
        bigger-map (conj the-map {:d 4})
        biggest-map (conj bigger-map {:e 5 :f 6})]
    biggest-map))

;; Expanding on maps (Records, like Elixir!)

(defrecord Book [title author page-count])
(defn there-are-records-which-are-almost-maps []
  (->Book "Dune" "Frank Herbert" 1e26))

;; Strings and chars.

(defn there-are-strings []
  "this is a\nmultiline string")

(defn strings-are-java-strings []
  "this is also
a multiline string")

(defn the-most-used-string-fn []
  (str "This function takes anything, like " 5 ", and concatonates! Here's the"
       "map: \n" (there-are-maps)))

(defn chars-are-a-thing []
  (str "Like many lisps, chars are denoted with a '\\' char, like so: " \a \b \c))

;; Booleans and nil

(defn careful-of-nil []
  (str "Unlike most lisps '() is NOT the same as nil and it is not falsy: "
       (if '() "see" "this doesn't come into the string")))

;; Basic functions

;; Multiple Arity
(defn greeting
  "Returns a greeting of the form 'Hello, username"
  ([] (greeting "world"))
  ([username]
   (str "Hello, " username)))

;; Variadic args
(defn date [person-1 person-2 & chaperones]
  (str person-1 " and " person-2
       " went out with "
       (count chaperones)
       " chaperones.\n"
       "Their names were: "
       (str/join ", " chaperones)))

;; Lambda expressions

;; Here's the example without lambdas from the book:
(defn indexable-word? [word]
  (> (count word) 2))

(defn get-indexable-words [sentance]
  (filter indexable-word? (str/split sentance #"\W+")))

(defn get-indexable-words-lambda [sentance]
  (filter (fn [w] (> (count w) 2)) (str/split sentance #"\W+")))

(defn get-indexable-words-succinct [sentance]
  (filter #(> (count %) 2) (str/split sentance #"\W+")))

;; And their values so we can return them as closures. Here is where clojure got
;; it's punny name!
(defn make-greeter [greeting-prefix]
  (fn [username] (str greeting-prefix ", " username)))

;; Bindings

;; let
(defn square-corners [bottom left size]
  (let [top (+ bottom size)
        right (+ left size)]
    [[bottom left] [top left] [top right] [bottom right]]))

;; Destructuring
(defn greet-author-1 [author]
  (str "Hello, " (:first-name author)))

(defn greet-author-2 [{fname :first-name}]
  (str "Hello, " fname))

(defn ellipsize [words]
  (let [[w1 w2 w3] (str/split words #"\s+")]
    (str/join " " [w1 w2 w3 "..."])))

;; Java land interop
(defn java-new-demo []
  (new java.util.Random))

(defn java-new-demo-concise []
  (java.util.Random.))

(defn use-a-random [^java.util.Random rnd]
  (. rnd nextInt 10))

;; Flow control

;; if
(defn is-small? [number]
  (if (< number 100) "yes" "no"))

;; do, chain multiple side effecting commands
(defn is-small-2? [number]
  (if (< number 100)
    "yes"
    (do
      (println "Saw a big Number" number)
      "no")))

;; Loop
(defn loops-yo []
  (loop [result []
         x 5]
    (if (zero? x)
      result
      (recur (conj result x) (dec x)))))

;; Recur can work without loop as well
(defn countdown [result x]
  (if (zero? x)
    result
    (recur (conj result x) (dec x))))

;; But this is rare since you can likely use clojure's included sequences and be
;; more declarative
(defn countdown-declaritive-1 [n]
  (into [] (take n (iterate dec 5))))

(defn countdown-declaritive-1 [n]
  (let [i (inc n)
        list-range (reverse (range i))]
    (into [] (drop-last list-range))))

(defn countdown-declaritive-2 [n]
  (vec (reverse (rest (range (inc n))))))

;; Where's my for loop

;; Here I reimplement indexOfAny from Apache Commons in clojure (actually I
;; copied it from the book but shut up)
(defn indexed [coll] (map-indexed vector coll))

(defn index-filter [pred coll]
  (when pred
    (for [[idx elt] (indexed coll) :when (pred elt)]
      idx)))

(defn index-of-any [pred coll]
  (first (index-filter pred coll)))

;; This is *way* more generic than the java version and can be used for loads!!
;; EG what is the 3rd heads coin flip in a series of coin flips?
(defn third-coinflip-heads []
  (nth (index-filter #{:h} [:t :t :h :t :h :t :t :t :h :h]) 3))
