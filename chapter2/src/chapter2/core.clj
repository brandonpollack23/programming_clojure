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
(defn make-greeter [greeting-prefix])
