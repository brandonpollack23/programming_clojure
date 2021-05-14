(ns snake.cljfx
  (:require [cljfx.api :as fx]
            [snake.core :refer :all])
  (:gen-class))

(defn game
  "Creates a new game in the javafx UI Runtime via cljfx"
  []
  (let [game-state (atom (reset-game {}))]))

(defn -main [& _]
  (println "Launching snake game in cljfx ui..."))
