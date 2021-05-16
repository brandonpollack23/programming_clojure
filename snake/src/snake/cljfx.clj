(ns snake.cljfx
  (:gen-class)
  (:require [cljfx.api :as fx]
            [snake.core :refer :all])
  (:import (javafx.scene.canvas Canvas GraphicsContext)
           (javafx.scene.paint Color)
           (javafx.scene.input KeyCode KeyEvent)))
;; Constants
(def canvas-width (* point-size width))
(def canvas-height (* point-size height))

(defn eightbit->float [n] (float (/ n 255)))
(def apple-color-rgba (map eightbit->float [210 50 90 255]))
(def snake-color-rgba (map eightbit->float [15 160 70 255]))

;; Drawing
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

;; Game loop
(defn draw-game
  "Draws game onto javafx canvas"
  [^Canvas c {:keys [snake apple]}]
  (let [g (.getGraphicsContext2D c)]
    (.clearRect g 0 0 canvas-width canvas-height)
    (paint g snake)
    (paint g apple)))

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

;; UI Events

(def keymap
  {KeyCode/LEFT  :left
   KeyCode/RIGHT :right
   KeyCode/UP    :up
   KeyCode/DOWN  :down
   KeyCode/A     :left
   KeyCode/D     :right
   KeyCode/S     :down
   KeyCode/W     :up})
;; TODO make pure event handler
(defn handle-key-pressed [state keycode]
  (if-let [direction (direction->vector (keymap keycode))]
    (update-direction state direction)
    state))

(defmulti handle :event/type)
(defmethod handle ::key-pressed [{:keys [app-state] :as event}]
  (let [keycode (.getCode ^KeyEvent (:fx/event event))]
    {:state (update
             app-state
             :game-state
             handle-key-pressed keycode)}))
;; (defn handle [{:keys [event/type app-state] :as event}]
;;   ;; TODO better destructuring
;;   (let [keycode (.getCode ^KeyEvent (:fx/event event))]
;;     (case type
;;       ::key-pressed {:state (update
;;                              app-state
;;                              :game-state
;;                              handle-key-pressed keycode)})))

(defn create-actual-handler [*state]
  (-> handle
      (fx/wrap-co-effects {:state #(deref *state)})
      (fx/wrap-effects {:state (fn [state _] (reset! *state state))})))

;; UI

(defn game-canvas [{:keys [game-state width height]}]
  {:fx/type :canvas
   :width width
   :height height
   ;; TODO on key pressed
   :draw #(draw-game % game-state)})

;; TODO on renderer exit exit application
(defn create-renderer [*application-state]
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
               :on-key-pressed {:event/type ::key-pressed
                                :app-state application-state}
               :root {:fx/type :v-box
                      :children [{:fx/type game-canvas
                                  :width canvas-width
                                  :height canvas-height
                                  :game-state (:game-state application-state)}]}}}))
   :opts {:fx.opt/map-event-handler (create-actual-handler *application-state)}))

(defn application-loop
  "Creates a new game in the javafx UI Runtime via cljfx"
  []
  (let [*application-state (atom {:game-state (reset-game)})
        renderer (fx/mount-renderer *application-state (create-renderer *application-state))]
    (loop []
      ;; TODO replace with something that isnt sleep
      (java.lang.Thread/sleep turn-millis)
      (swap! *application-state application-game-step)
      (recur))))

(defn -main [& _]
  (println "Launching snake game in cljfx ui...")
  (application-loop))
