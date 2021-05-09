(ns chapter3.core
  (:require [clojure.string :as str]
            [clojure.java.io :refer [reader]]
            [clojure.set :as set])
  (:import java.io.File))

(def whole-numbers (iterate inc 1))

(def cycles (cycle (take 5 whole-numbers)))
(def cycles-finitized (take 20 cycles))

(def interleaved (interleave whole-numbers ["A" "B" "C"]))

(def interposed (interpose "," ["apples" "bananas" "grapes"]))

;; List comprehension
(def while-for
  (take 10 (for [n whole-numbers :when (even? n)] n)))

;; This works as a set product starting with iterating the rightmost
(def combining-fors
  (for [file "ABCDEFGH"
        rank (range 1 9)]
    (format "%c%d" file rank)))

(defn quick-sort-lazy [[pivot & coll]]
  (when pivot
    (let [lower (for [x coll :when (< x pivot)] x)
          upper (for [x coll :when (> x pivot)] x)]
      (lazy-cat (quick-sort-lazy lower) [pivot] (quick-sort-lazy upper)))))
(defn quick-sort [coll]
  (doall (quick-sort-lazy coll)))

;; Lazy and infinite sequences

;; Prime #'s with wheel factorization
(def primes
  (concat
   [2 3 5 7]
   (lazy-seq
    (let [primes-from
          (fn primes-from [n [f & r]]
            (if (some #(zero? (rem n %))
                      (take-while #(<= (* % %) n) primes))
              (recur (+ n f) r)
              (lazy-seq (cons n (primes-from (+ n f) r)))))
          wheel (cycle [2 4 2 4 6 2 6 4 2 4 6 6 2 6  4  2
                        6 4 6 8 4 2 4 2 4 8 6 4 6 2  4  6
                        2 6 6 4 2 4 6 2 6 4 2 4 2 10 2 10])]
      (primes-from 11 wheel)))))

;; Java collections are seqable
(def hello-seqable
  (let [hello (map char (cons (int \h) (.getBytes "ello")))]
    (apply str hello)))

;; Regex

;; DONT DO THIS
(defn bad-regex []
  (let [m (re-matcher #"\w+" "the quick brown fox")]
    (loop [match (re-find m)]
      (when match
        (println match)
        (recur (re-find m))))))

;; use seqs instead!
(def good-regex
  (re-seq #"\w+" "the quick brown fox"))
(defn print-good-regex []
  (println (str/join "\n" good-regex)))

;; Fileysystem is a seq too!
(defn getfileshere []
  (.listFiles (File. ".")))

(defn do-file-seqs [path]
  (file-seq (File. path)))

(defn minutes-to-millis [mins] (* mins 1000 60))
(defn recently-modified? [^File file]
  (> (.lastModified file)
     (- (System/currentTimeMillis) (minutes-to-millis 30))))
(defn get-recent-files [path]
  (filter recently-modified? (file-seq (File. path))))

;; Seq over stream

;; Bad, leaves reader open.  When using resources gotta close them
(defn bad-read-a-stream []
  (line-seq (reader "src/chapter3/core.clj")))

(defn read-a-stream []
  (with-open [line-reader (reader "src/chapter3/core.clj")]
    (line-seq line-reader)))

;; Excersizes
(defn non-blank? [line]
  (not (str/blank? line)))

(defn non-svn? [file]
  (not (-> file
           .toString
           (.endsWith ".svn"))))

(defn clojure-source? [file]
  (let [ending-clojure
        (fn ending-clojure [fn]
          (or (.endsWith fn ".clj")
              (.endsWith fn ".cljs")
              (.endsWith fn ".cljc")))]
    (-> file
        .toString
        ending-clojure)))

(defn clojure-loc
  "Returns all the lines of code of the checked out files that are for clojure,
  clojurescript, or either"
  [path]
  (let [files (filter #(.isFile %) (file-seq (File. path)))]
    (apply
     +
     (for [f files :when (and (clojure-source? f) (non-svn? f))]
       (with-open [rdr (reader f)]
         (count (filter non-blank? (line-seq rdr))))))))

;; Structure specific functions.

;; Vecs and Lists were obvious, maps are pretty simple too
;; assoc, dissoc, get, map as function, keyword as function
;;
;; One subtelty is if a map contains a key whose value is nil, you can differentiate that with contains?
;;
;; On merge, rightmost wins
;; merge-with allows specificity of what should happen

(def song {:name "Agnus Dei"
           :artist "Krzystzof Penderecki"
           :album "Polish Requiem"
           :genre "Classical"})

(merge-with
 #(str %1 " " %2)
 song {:size 8118166 :time 507245 :genre "Shit"})

;; Set functions
;; There are more in clojure.set!
(def languages #{"java" "c" "d" "clojure"})
(def beverages #{"java" "chai" "pop"})

(set/union languages beverages)
(set/intersection languages beverages)
(set/difference languages beverages)
;; Different from filter because it returns a set, not a seq
(set/select #(= 1 (count %)) languages)

;; Clojure sets have everything we need for relational algebra implementation
;; and therefore an "in memory database"
(def compositions
  #{{:name "The Art of the Fugue" :composer "J. S. Bach"}
    {:name "Musical Offering" :composer "J. S. Bach"}
    {:name "Requiem" :composer "Giuseppe Verdi"}
    {:name "Requiem" :composer "W. A. Mozart"}
    {:name "Fake Song" :composer "Nobody"}})
(def composers
  #{{:composer "J. S. Bach" :country "Germany"}
    {:composer "W. A. Mozart" :country "Germany"}
    {:composer "Giuseppe Verdi" :country "Italy"}
    {:composer "Fake Guy" :country "Space"}})
(def nations
  #{{:nation "Germany" :language "German"}
    {:nation "Austria" :language "German"}
    {:nation "Italy" :language "Italian"}})

;; Rename renames keys
(set/rename compositions {:name :title})
(set/select #(= (:name %) "Requiem") compositions)

;; Project calls select-keys on each member, like select in sql with certain columns
(set/project compositions [:name])

;; Cross product is possible with for like so, but usually a subset is wanted,
;; which can be achieved with join
(for [m compositions c composers] (concat m c))
(set/join compositions composers)
(set/join nations composers {:country :nation})

(set/project
 (let [reqiuems (set/select #(= (:name %) "Requiem") compositions)]
   (set/join
    reqiuems
    composers))
 [:country])

(defn left-join
  [xrel yrel]
  (let [xcols (set (keys (first (seq xrel))))
        ycols (set (keys (first (seq yrel))))
        shared-keys (set/intersection xcols ycols)
        ;; Get an index map of the set of shared keys to entries in yrel
        idx (set/index yrel shared-keys)]
    (reduce (fn [acc x]
              ;; Try to find a row containing all the shared keys in y, which is
              ;; the key to the index map.
              (if-let [found-ys (idx (select-keys x shared-keys))]
                ;; Found in y, merge with x and add.
                ;; For every found y, merge it into acc
                (reduce #(conj %1 (merge x %2)) acc found-ys)
                ;; Not in y, left join by just adding the x row
                (conj acc x)))
            #{} xrel)))

(defn right-join [xrel yrel]
  (left-join yrel xrel))
