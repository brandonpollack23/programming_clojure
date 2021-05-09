(defproject chapter5 "0.1.0-SNAPSHOT"
  :description "Chapter5 Programming Clojure 3rd edition"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot chapter5.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns chapter4.core
                 :init (do
                         (set! *print-length* 10)
                         (set! *print-level* 25))})
