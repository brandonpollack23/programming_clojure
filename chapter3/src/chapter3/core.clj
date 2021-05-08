(ns chapter3.core
  (:require [clojure.string :as str]
            [clojure.java.io :refer [reader]])
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
