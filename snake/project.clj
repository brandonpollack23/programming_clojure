(defproject snake "0.1.0-SNAPSHOT"
  :description "Chapter 6 Project Snake game"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :target-path "target/%s"
  :main snake.swing
  :profiles {:uberjar-swing {:aot :all}
             :dev {:dependencies [[org.baznex/imports "1.4.0"]]
                   :global-vars [[*print-length* 10]
                                 [*print-level* 25]]
                   :repl-options {:init-ns snake.swing}
                   :injections [(require '[clojure.spec.test.alpha :as stest])
                                (stest/instrument)]}
             ;; TODO (uber)jar-exclusions of swing
             :cljfx {:dependencies [[cljfx "1.7.13"]]
                     :global-vars [[*print-length* 10]
                                   [*print-level* 25]]
                     :repl-options {:main snake.cljfx
                                    :init-ns snake.cljfx}
                     :injections [(require '[clojure.spec.test.alpha :as stest])
                                  (stest/instrument)]}})
