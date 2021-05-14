(defproject snake "0.1.0-SNAPSHOT"
  :description "Chapter 6 Project Snake game"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.baznex/imports "1.4.0"]]
  :target-path "target/%s"
  :main snake.swing
  :profiles {:uberjar-swing {:aot :all}
             :dev {:repl-options {:init-ns snake.swing
                                  :init (do
                                          (set! *print-length* 10)
                                          (set! *print-level* 25))}
                   :injections [(require '[clojure.spec.test.alpha :as stest])
                                (stest/instrument)]}})
