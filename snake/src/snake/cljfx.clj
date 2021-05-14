(ns snake.cljfx
  (:gen-class)
  (:require [cljfx.api :as fx]
            [snake.core :refer :all])
  (:import (javafx.scene.canvas Canvas GraphicsContext)
           (javafx.scene.paint Color)))

(defn eightbit->float [n] (float (/ n 255)))
(def apple-color-rgba (map eightbit->float [210 50 90 1]))
(def snake-color-rgba (map eightbit->float [15 160 70 1]))

(def game-colors->javafx-color {:apple-color (apply #(Color. %1 %2 %3 %4) apple-color-rgba)
                                :snake-color (apply #(Color. %1 %2 %3 %4) snake-color-rgba)})
(defn fill-point [^GraphicsContext g ^Color color pt]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setFill g (game-colors->javafx-color color))
    (.fillRect g x y width height)))

(defmulti paint (fn [^GraphicsContext g object & _] (:type object)))
(defmethod paint :apple [^GraphicsContext g {:keys [location color]}]
  (fill-point g color location))
(defmethod paint :snake [^GraphicsContext g {:keys [body color]}]
  (doseq [point body]
    (fill-point g color point)))

(defn draw-game
  "Draws game onto javafx canvas"
  [^Canvas c {:keys [snake apple]}]
  (let [g (.getGraphicsContext2D c)]
    (.clearRect g 0 0 width height)
    (paint g snake)
    (paint g apple)))

(defn game-canvas [{:keys [game-state width height]}]
  {:fx/type :canvas
   :width width
   :height height
   :draw #(draw-game % game-state)})

(def window-width (* point-size width))
(def window-height (* point-size height))
(def renderer
  (fx/create-renderer
   :middleware
   (fx/wrap-map-desc
    (fn [game-state]
      {:fx/type :stage
       :title "CLJFX Snake!"
       :width window-width
       :height window-height
       :showing true
       :scene {:fx/type :scene
               :root {:fx/type :v-box
                      :alignment :center
                      :children [{:fx/type game-canvas
                                  :width width
                                  :height height
                                  :game-state game-state}]}}}))))

(defn game
  "Creates a new game in the javafx UI Runtime via cljfx"
  []
  (let [*game-state (atom (reset-game {}))]
    (fx/mount-renderer *game-state renderer)))

(defn -main [& _]
  (println "Launching snake game in cljfx ui..."))
