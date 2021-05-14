(ns snake.core
  (:import (java.awt Color Graphics Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:require [org.baznex.imports :refer [import-static]]
            [clojure.spec.alpha :as s])
  (:gen-class))
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

;; Specs
(s/def ::coordinate (s/tuple int? int?))
(s/def ::locaton ::coordinate)
(s/def ::rect (s/cat :x int? :y int? :l int? :w int?))
(s/def ::direction (s/and (s/tuple int? int?) #(#{[0 1] [0 -1] [1 0] [-1 0]} %)))
(s/def ::body (s/coll-of ::coordinate :distinct true :into '()))

(s/def ::canvas-object (s/keys :req-un [::type]))
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
   :color (Color. 210 50 90)
   :type :apple})

(s/fdef create-snake
  :ret ::snake)
(defn create-snake
  "Creates the snake with a body of size 1, facing right"
  []
  {:body (list [1 1])
   :dir [1 0]
   :type :snake
   :color (Color. 15 160 70)})

(s/fdef turn
  :args (s/cat :snake ::snake :direction ::direction)
  :ret ::snake)
(defn turn
  "Turns the direction of the snake"
  [snake newdir]
  (assoc snake :dir newdir))

(s/fdef move
  :args (s/cat :snake ::snake :grow (s/? symbol?))
  :ret ::snake)
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

;; Swing GUI
;;
(defn fill-point [^:Graphics g ^:Color color pt]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g color)
    (.fillRect g x y width height)))

(defmulti paint (fn [^:Graphics g object & _] (:type object)))
(defmethod paint :apple [^:Graphics g {:keys [location color]}]
  (fill-point g color location))
(defmethod paint :snake [^:Graphics g {:keys [body color]}]
  (doseq [point body]
    (fill-point g color point)))

(defn game-panel [^:JFrame frame game-state]
  (proxy [JPanel ActionListener KeyListener] []
    (paintComponent [^:Graphics g]
      (proxy-super paintComponent g)
      (paint g (@game-state :snake))
      (paint g (@game-state :apple)))
    (actionPerformed [e]
      (swap! game-state update-positions)
      (when (head-overlaps-body? (@game-state :snake))
        (swap! game-state reset-game)
        (JOptionPane/showMessageDialog frame "You lose!"))
      (when (win? (@game-state :snake))
        (swap! game-state reset-game)
        (JOptionPane/showMessageDialog frame "You win!"))
      (.repaint this))
    (keyPressed [e]
      (swap! game-state update-direction (dirs (.getKeyCode e))))
    (getPreferredSize []
      (Dimension. (* (inc width) point-size)
                  (* (inc height) point-size)))
    (keyReleased [e])
    (keyTyped [e])))

(defn game
  "Creates a new game in the swing UI Runtime"
  []
  (let [game-state (atom (reset-game {}))
        frame (JFrame. "Snake")
        panel (game-panel frame game-state)
        timer (Timer. turn-millis panel)]
    (doto panel
      (.setFocusable true)
      (.addKeyListener panel))
    (doto frame
      (.add panel)
      (.pack)
      (.setVisible true))
    (.start timer)
    [game-state, timer]))

(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (println "Launching snake game in swing ui...")
  (game))
