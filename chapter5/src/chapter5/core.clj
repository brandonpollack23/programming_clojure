(ns chapter5.core
  (:require [clojure.spec.alpha :as s]
            [clojure.core.reducers :as r]
            [clojure.spec.test.alpha :as stest]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]))

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
(defn rand-str [len] (let [rand-char (fn [] (char ((rand-nth [#(+ (rand 26) (int \A))
                                                              #(+ (rand 26) (int \a))]))))]
                       (->> rand-char
                            (repeatedly len)
                            (apply str))))

;; This is exemplefied here, go ahead and evaluate this, then time line 99 in teh repl, itll be fast every time.
(s/def ::sampling-scores (s/every-kv string? int?))
(def random-mapping (into {}
                          (take 1000 (repeatedly #(vector
                                                   (rand-str (+ 1 (rand 100)))
                                                   (int (rand 10000)))))))
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

;; Tuples
(s/def ::point (s/tuple float? float?))
(s/conform ::point [1.3 2.7])

;; Maps
(def example-map-structure
  {:music/id #uuid "a893e514-581f-41bd-bcc8-3cebc2f3a676"
   :music/artist "Rush"
   :music/title "Moving Pictures"
   :music/date #inst "1981-02-12"})

;; ;; Now lets spec it
(s/def :music/id uuid?)
(s/def :music/artist string?)
(s/def :music/title string?)
(s/def :music/date inst?)
(s/def :music/release
  (s/keys :req [:music/id]
          :opt [:music/artist
                :music/title
                :music/date]))
(s/conform :music/release example-map-structure)
;; Extras are ok and conform as well
(def example-map-structure-extra
  (assoc example-map-structure :music/extra 'extra-new-stuff))
(s/valid? :music/release example-map-structure-extra)
(s/conform :music/release example-map-structure-extra)

;; with unqualified names its like this:
(s/def :music/release-unqualified
  (s/keys :req-un [:music/id]
          :opt-un [:music/artist
                   :music/title
                   :music/date]))
(def example-map-structure-unqualified
  {:id #uuid "a893e514-581f-41bd-bcc8-3cebc2f3a676"
   :artist "Rush"
   :title "Moving Pictures"
   :date #inst "1981-02-12"})
(s/valid? :music/release-unqualified example-map-structure-unqualified)

;; regex ops
(s/def ::cat-example (s/cat :s string? :i int?))
(s/valid? ::cat-example ["abc" 100])
(s/conform ::cat-example ["abc" 100])

;; or equiv to (op1|op2)
(s/def ::alt-example (s/alt :i int? :vi (s/coll-of int?) :k keyword?))
(s/valid? ::alt-example [[100 200]])
(s/valid? ::alt-example [:one-hundred])
(s/conform ::alt-example [:one-hundred])

;; repitition + ? *
(s/def ::oe (s/cat :odds (s/+ odd?) :even (s/? even?)))
(s/conform ::oe [1 3 5 10])
(s/conform ::oe [1 3 5 10 12]) ;; false

;; variable arguments with regex ops
;; eg any number of any type
(s/def ::println-args (s/* any?))

;; variadic args, like for clojure.set/intersection
(s/def ::intersection-args (s/cat :s1 set? :sets (s/* set?)))
;; or
(s/def ::intersection-args-2 (s/+ set?))

;; and options keywords list like the atom fn
(s/def ::meta map?)
(s/def ::validator ifn?)
(s/def ::atom-args
  (s/cat :x any? :options (s/keys* :opt-un [::meta ::validator])))
(s/conform ::atom-args [100 :meta {:foo 1} :validator int?])

;; Example for repeat, which has an optional first arg integer
(s/def ::repeat-args
  (s/cat :n (s/? int?) :x any?))
(s/conform ::repeat-args [5 'test])
(s/conform ::repeat-args ['test])

;; Finally, specifying functions, let's do it for rand:
;; Returns a random floating point number between 0 (inclusive) and
;; n (default 1) (exclusive).
(s/def ::rand-args (s/cat :n (s/? number?)))
(s/def ::rand-ret double?)
;; Relates args to ret
(s/def ::rand-fn
  (fn [{:keys [args ret]}]
    (let [n (or (:n args) 1)]
      (cond (zero? n) (zero? ret)
            (pos? n) (and (>= ret 0) (< ret n))
            (neg? n) (and (<= ret 0) (> ret n))))))
(s/fdef clojure.core/rand
  :args ::rand-args
  :ret  ::rand-ret
  :fn   ::rand-fn)

;; This still is not instrumented but we will talk about anon functions before
;; moving on
(defn opposite [pred]
  (comp not pred))
(s/def ::pred
  (s/fspec
   :args (s/cat :x any?)
   :ret boolean?))

(s/fdef opposite
  :args (s/cat :pred ::pred)
  :ret ::pred)

;; Instrumenting (most likely in tests)
(stest/instrument 'clojure.core/rand)

;; Now lets do generative testing with something like (doc symbol)
;; clojure.core/symbol
;; ([name] [ns name])
;; Returns a Symbol with the given namespace and name. Arity-1 works
;; on strings, keywords, and vars.
(s/fdef clojure.core/symbol
  :args (s/cat :ns (s/? string?) :name string?)
  :ret symbol?
  :fn (fn [{:keys [args ret]}]
        (and (= (name ret) (:name args))
             (= (namespace ret) (:ns args)))))
;; Now run against 100 generated tests.
(stest/check 'clojure.core/symbol)
;; Or generate myself
(s/exercise (s/cat :ns (s/? string?) :name string?))

;; Sometimes you need a custom generator.
;; Take the big-odd example here:
(defn big? [x] (> x 100))
(s/def ::big-odd (s/and odd? big?))
;; (s/exercise ::big-odd) ;; boom!

;; This is because there is no mapped predicate for odd?, but if we add one first:
(s/def ::big-odd (s/and int? odd? big?))
;; it works out.  That's because it generates an int and checks the rest, which
;; can get pretty slow for very stringent parameters, for this we can customize
;; the generator.

;; Here is one, for the very first function where we generated marbles.
(s/def :marble/color-red (s/with-gen :marble/color #(s/gen #{:red})))
;; Now it only generates red, useful for narrowing down a type of test.
(s/exercise :marble/color-red)
;; we can sldo map generators!
(s/def ::sku
  (s/with-gen (s/and string? #(str/starts-with? % "SKU-"))
    (fn [] (gen/fmap #(str "SKU-" %) (s/gen (s/and string? #(> (.length %) 0)))))))
(s/exercise ::sku)
