(ns snake.core
  (:import (java.awt Color Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:require [org.baznex.imports :refer [import-static]]
            [clojure.spec.alpha :as s]))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)

;; Global configuration options.
(def width 75)
(def height 50)
(def point-size 10)
(def turn-millis 75)
(def win-length 5)
(def dirs {VK_LEFT  [-1 0]
           VK_RIGHT [1 0]
           VK_UP    [0 -1]
           VK_DOWN  [0 1]})

(s/def ::coordinate (s/tuple int? int?))
(s/def ::rect (s/cat :x int? :y int? :l int? :w int?))
(s/def ::canvas-object (s/keys :req-un [::type]))
(s/def ::direction (s/and (s/tuple int? int?) #(#{[0 1] [0 -1] [1 0] [-1 0]} %)))

(s/fdef add-points
  :args (s/* ::coordinate)
  :ret ::coordinate)
(defn add-points
  "Add coordinate points together"
  [& pts]
  (vec (apply map + pts)))

(s/fdef point-to-screen-rect
  :args ::coordinate
  :ret ::rect)
(defn point-to-screen-rect
  "Converts a point in game space to a rectange on the screen"
  [pt]
  (map #(* point-size %)
       [(pt 0) (pt 1) 1 1]))

(s/fdef create-apple
  :ret ::canvas-object)
(defn create-apple
  "Creates an apple with a random x y coordinate within width and height"
  []
  {:location [(rand-int width) (rand-int height)]
   :color (Color. 210 50 90)
   :type :apple})

(s/fdef create-snake
  :ret ::canvas-object)
(defn create-snake
  "Creates the snake with a body of size 1, facing right"
  []
  {:body (list [1 1])
   :dir [1 0]
   :type :snake
   :color (Color. 15 160 70)})

(s/fdef turn
  :args (s/tuple ::canvas-object ::direction)
  :ret ::canvas-object)
(defn turn
  "Turns the direction of the snake"
  [snake newdir]
  (assoc snake :dir newdir))

(s/fdef move
  :args (s/and (s/keys :req-un [::body ::dir]) (s/? symbol?))
  :ret ::canvas-object)
(defn move
  "Moves the snake, potentially growing it (by not removing the tail)"
  [{:keys [body dir] :as snake} & grow]
  (assoc snake :body
         (cons (add-points (first body) dir)
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

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
