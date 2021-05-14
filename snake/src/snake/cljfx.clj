(ns snake.cljfx
  (:gen-class)
  (:require [cljfx.api :as fx]
            [snake.core :refer :all])
  (:import (javafx.scene.canvas Canvas GraphicsContext)
           (javafx.scene.paint Color)))
(def canvas-width (* point-size width))
(def canvas-height (* point-size height))

(defn eightbit->float [n] (float (/ n 255)))
(def apple-color-rgba (map eightbit->float [210 50 90 255]))
(def snake-color-rgba (map eightbit->float [15 160 70 255]))

(def game-colors->javafx-color {:apple-color (apply #(Color. %1 %2 %3 %4) apple-color-rgba)
                                :snake-color (apply #(Color. %1 %2 %3 %4) snake-color-rgba)})
(defn fill-point [^GraphicsContext g ^Color color pt]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setFill g (game-colors->javafx-color color))
    (.fillRect g x y width height)))

(defmulti paint (fn [^GraphicsContext _ object & _] (:type object)))
(defmethod paint :apple [^GraphicsContext g {:keys [location color]}]
  (fill-point g color location))
(defmethod paint :snake [^GraphicsContext g {:keys [body color]}]
  (doseq [point body]
    (fill-point g color point)))

(defn draw-game
  "Draws game onto javafx canvas"
  [^Canvas c {:keys [snake apple]}]
  (let [g (.getGraphicsContext2D c)]
    (.clearRect g 0 0 canvas-width canvas-height)
    (paint g snake)
    (paint g apple)))

(defn game-canvas [{:keys [game-state width height]}]
  {:fx/type :canvas
   :width width
   :height height
   :draw #(draw-game % game-state)})

;; TODO on renderer exit exit application
(def renderer
  (fx/create-renderer
   :middleware
   (fx/wrap-map-desc
    (fn [application-state]
      {:fx/type :stage
       :title "CLJFX Snake!"
       :width canvas-width
       :height canvas-height
       :showing true
       :scene {:fx/type :scene
               :root {:fx/type :v-box
                      :children [{:fx/type game-canvas
                                  :width canvas-width
                                  :height canvas-height
                                  :game-state (:game-state application-state)}]}}}))))

(defn game-step
  [snake next-state]
  (cond
    (head-overlaps-body? snake) (do
                                  ;; TODO replace with popup
                                  (println "YOU LOSE!")
                                  (reset-game))
    (win? snake) (do
                   (println "YOU WIN!")
                   (reset-game))
    :else next-state))

(defn application-game-step [{:keys [game-state] :as application-state}]
  (assoc application-state :game-state
         (let [{:keys [snake] :as next-state}
               (update-positions game-state)]
           (game-step snake next-state))))

(defn application-loop
  "Creates a new game in the javafx UI Runtime via cljfx"
  []
  (let [*application-state (atom {:game-state (reset-game)})
        renderer (fx/mount-renderer *application-state renderer)]
    (loop []
      ;; TODO event handling
      ;; TODO replace with something that isnt sleep
      (java.lang.Thread/sleep turn-millis)
      (swap! *application-state application-game-step)
      (recur))))

(defn -main [& _]
  (println "Launching snake game in cljfx ui...")
  (application-loop))
