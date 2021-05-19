(defproject snake "0.1.0-SNAPSHOT"
  :description "Chapter 6 Project Snake game"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [cljfx "1.7.13"]]
  :target-path "target/%s"
  :main snake.cljfx
  :profiles {:uberjar-cljfx {:aot [snake.core
                                   snake.cljfx]
                             :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]
                             :uberjar-name "snake-cljfx-standalone.jar"}
             :dev {:dependencies [[clj-commons/pomegranate "1.2.1"]
                                  [vvvvalvalval/scope-capture "0.3.2"]]
                   :global-vars [[*print-length* 10]
                                 [*print-level* 25]]
                   :repl-options {:main snake.cljfx
                                  :init-ns snake.cljfx}
                   :injections [(require '[clojure.spec.test.alpha :as stest])
                                (stest/instrument)
                                (require 'sc.api)]}
             :swing {:dependencies [[org.baznex/imports "1.4.0"]]
                     :global-vars [[*print-length* 10]
                                   [*print-level* 25]]
                     :repl-options {:init-ns snake.swing}
                     :injections [(require '[clojure.spec.test.alpha :as stest])
                                  (stest/instrument)]}})
