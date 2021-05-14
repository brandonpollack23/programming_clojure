(ns snake.swing
  (:import (java.awt Color Graphics Dimension)
           (javax.swing JPanel JFrame Timer JOptionPane)
           (java.awt.event ActionListener KeyListener))
  (:require [org.baznex.imports :refer [import-static]]
            [snake.core :refer :all])
  (:gen-class))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN)

(def game-colors->swing-color {:apple-color (Color. 210 50 90)
                               :snake-color (Color. 15 160 70)})
(defn fill-point [^:Graphics g ^:Color color pt]
  (let [[x y width height] (point-to-screen-rect pt)]
    (.setColor g (game-colors->swing-color color))
    (.fillRect g x y width height)))

(defmulti paint (fn [^:Graphics g object & _] (:type object)))
(defmethod paint :apple [^:Graphics g {:keys [location color]}]
  (fill-point g color location))
(defmethod paint :snake [^:Graphics g {:keys [body color]}]
  (doseq [point body]
    (fill-point g color point)))

(def keystate->dirs {VK_LEFT  :left
                     VK_RIGHT :right
                     VK_UP    :up
                     VK_DOWN  :down})
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
      (swap! game-state update-direction (->> (.getKeyCode e)
                                              keystate->dirs
                                              direction->vector)))
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
