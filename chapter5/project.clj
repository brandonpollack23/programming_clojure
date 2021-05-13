(defproject chapter5 "0.1.0-SNAPSHOT"
  :description "Chapter5 Programming Clojure 3rd edition"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/test.check "1.1.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:repl-options {:init-ns chapter5.core
                                  :init (do
                                          (set! *print-length* 10)
                                          (set! *print-level* 25))}
                   :injections [(require 'clojure.spec.test.alpha)
                                (clojure.spec.test.alpha/instrument)]}})
