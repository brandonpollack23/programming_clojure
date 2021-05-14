(ns snake.core
  (:require [clojure.spec.alpha :as s]))

;; Global configuration options.
(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def win-length 5)
(def direction->vector {:left  [-1 0]
                        :right [1 0]
                        :up    [0 -1]
                        :down  [0 1]})

;; Specs
(s/def ::coordinate (s/tuple int? int?))
(s/def ::locaton ::coordinate)
(s/def ::rect (s/cat :x int? :y int? :l int? :w int?))
(s/def ::direction (s/and (s/tuple int? int?) #{[0 1] [0 -1] [1 0] [-1 0]}))
(s/def ::body (s/coll-of ::coordinate :distinct true :into '()))

(s/def ::canvas-object (s/keys :req-un [::type ::color]))
(s/def ::snake (s/merge ::canvas-object (s/keys :req-un [::body ::direction])))
(s/def ::apple (s/merge ::canvas-object (s/keys :req-un [::location])))

(s/def ::game-state (s/keys :req [::snake ::apple]))

;; Functional Model

(s/fdef add-points
  :args (s/* ::coordinate)
  :ret ::coordinate)
(defn add-points
  "Add coordinate points together"
  [& pts]
  (vec (apply map + pts)))

(s/fdef point-to-screen-rect
  :args (s/cat :coordinate ::coordinate)
  :ret ::rect)
(defn point-to-screen-rect
  "Converts a point in game space to a rectange on the screen"
  [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

(s/fdef create-apple
  :ret ::apple)
(defn create-apple
  "Creates an apple with a random x y coordinate within width and height"
  []
  {:location [(rand-int width) (rand-int height)]
   :color :apple-color
   :type :apple})

(s/fdef create-snake
  :ret ::snake)
(defn create-snake
  "Creates the snake with a body of size 1, facing right"
  []
  {:body (list [1 1])
   :direction [1 0]
   :type :snake
   :color :snake-color})

(s/fdef turn
  :args (s/cat :snake ::snake :direction ::direction)
  :ret ::snake)
(defn turn
  "Turns the direction of the snake"
  [snake newdir]
  (assoc snake :direction newdir))

(s/fdef move
  :args (s/cat :snake ::snake :grow (s/? keyword?))
  :ret ::snake)
(defn move
  "Moves the snake, potentially growing it (by not removing the tail)"
  [{:keys [body direction] :as snake} & grow]
  (assoc snake :body
         (cons (add-points (first body) direction)
               (if grow body (butlast body)))))

(defn head-overlaps-body?
  "Whether the snake ran into itself"
  [{[head & body] :body}]
  ((set body) head))

(defn eats?
  "Whether snake overlaps apple and eats it"
  [{[snake-head & _] :body} {apple :location}]
  (= snake-head apple))

(defn win?
  "Whether or not you won the game"
  [{body :body}]
  (>= (count body) win-length))

;; Begin Mutable Model

(defn reset-game [_]
  {:snake (create-snake)
   :apple (create-apple)})

(defn update-direction [{:keys [snake] :as game-state} newdir]
  (if newdir
    (assoc game-state :snake (turn snake newdir))
    game-state))

(defn update-positions [{:keys [snake apple] :as game-state}]
  (if (eats? snake apple)
    (assoc game-state :apple (create-apple) :snake (move snake :grow))
    (assoc game-state :snake (move snake))))
