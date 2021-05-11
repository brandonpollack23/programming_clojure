(ns chapter5.core
  (:require [clojure.spec.alpha :as s]
            [clojure.core.reducers :as r]))

(s/def ::company-name string?)

;; Can be used like so:
(s/valid? ::company-name "Acme Moving")
(s/valid? ::company-name 100)

;; Selling marbles!
;; Sets implement function interface, so they can be used for options!
(s/def :marble/color #{:red :green :blue})
(s/valid? :marble/color :red)
(s/valid? :marble/color :pink)

;; We can match numbers too
(s/def :bowling/roll-set #{0 1 2 3 4 5 6 7 8 9})
(s/def :bowling/roll-made-set (into #{} (range 11)))

(s/def :bowling/roll (s/int-in 0 11))
(s/valid? :bowling/roll 10)

(s/def :something-else/doublevar (s/double-in 0.0 5.5))
(s/def :something-else/timevars (s/inst-in (java.util.Date.) (java.util.Date.)))

;; Nil is falsy, but sometimes we want to allow it.
(s/def ::company-name-2 (s/nilable string?))
(s/valid? ::company-name-2 nil)
(s/valid? ::company-name-2 "Google")
(s/valid? ::company-name-2 42) ;; False

;; Want something to be a boolean or nil? use nilable
(s/def ::nilable-boolean (s/nilable boolean?))
(s/valid? ::nilable-boolean true)
(s/valid? ::nilable-boolean false)
(s/valid? ::nilable-boolean nil)
(s/valid? ::nilable-boolean 37) ;; False

;; Logical combinations
(s/def ::odd-int (s/and int? odd?))
(s/valid? ::odd-int 37)
(s/valid? ::odd-int 40) ;; False
(s/valid? ::odd-int 42) ;; False

(s/def ::odd-int-or-42 (s/or :odd ::odd-int
                             :is-42 #{42}))
(s/valid? ::odd-int-or-42 37)
(s/valid? ::odd-int-or-42 40) ;; False
(s/valid? ::odd-int-or-42 42)

;; Tells you all the ways (with labels) you conformed.
(s/conform ::odd-int-or-42 42)
;; Tells you all the ways you didn't conform
(s/explain-str ::odd-int-or-42 0)
;; Same but just prints
(s/explain ::odd-int-or-42 0)

;; Now we can also validate collections!
(s/def ::names (s/coll-of string?))
(s/valid? ::names ["Alex" "Stu"])
(s/valid? ::names {"Alex" "Stu"})
(s/valid? ::names #{"Alex" "Stu"})
(s/valid? ::names [1 "Alex" "Stu"]) ;; false

;; Extra params
(s/def ::my-set (s/coll-of int? :kind set? :min-count 2))
(s/valid? ::my-set #{1 2})
(s/valid? ::my-set #{1 2 3})
(s/explain-str ::my-set #{1}) ;; false, too small
(s/explain-str ::my-set [1 2]) ;; false, not a set
(s/explain-str ::my-set #{'a 'b}) ;; false, not ints


;; There is an even more powerful map-of
(s/def ::scores (s/map-of string? int?))
(s/valid? ::scores {"Stu" 37 "Alex" 42})
(s/explain ::scores {'Stu 37 "Alex" 42}) ;; false, not all {str -> int}

(s/def ::nesting (s/map-of string? ::scores))
(s/conform ::nesting {"game1" {"Stu" 37 "Alex" 42}
                      "game2" {"Brandon" 37 "Billy" 42}})

;; it always is a map in conform
(s/def ::nesting-or-42 (s/or :map ::nesting :is-42 #{42}))
(s/conform ::nesting-or-42 {"game1" {"Stu" 37 "Alex" 42}
                            "game2" {"Brandon" 37 "Billy" 42}})
(s/conform ::nesting-or-42 42)

;; Sampling collections check up to s/*coll-check-limit* elements
(defn rand-str [len] (let [rand-char #(char (rand-nth [(+ (rand 26) (int \A))
                                                       (+ (rand 26) (int \a))]))]
                       (apply str (take len (repeatedly rand-char)))))

;; This is exemplefied here, go ahead and evaluate this, then time line 99 in teh repl, itll be fast every time.
(s/def ::sampling-scores (s/every-kv string? int?))
(def random-mapping (into {}
                          (take 1000 (repeatedly #(vector
                                                   (rand-str 10)
                                                   (int (rand 1000)))))))
(def random-mapping-2 (into {}
                            (map (fn [_] (vector (rand-str 10) (int (rand 1000)))))
                            (range 1000)))
(def random-mapping-3
  (->> (make-array (type (vector)) 1000)
       (r/map (fn [_] (vector (rand-str 10) (int (rand 1000)))))
       (into {})))

(def random-mapping-4 (let [chunk-size (.availableProcessors (Runtime/getRuntime))
                            futures (for [_ (range chunk-size)]
                                      (future (->> (make-array (type (vector)) (/ 1000 chunk-size))
                                                   (r/map (fn [_] (vector (rand-str 10) (int (rand 1000)))))
                                                   (into {}))))]
                        (->> futures
                             (r/map deref)
                             (r/fold merge))))

(s/valid? ::sampling-scores random-mapping)
