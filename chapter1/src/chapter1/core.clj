(ns chapter1.core)

(defn hello
  "My first function in clojure"
  [name]
  (str "Hello, " name))

(def visitors (atom #{}))
(defn hello-stateful
  "A version of the hello function above that keeps track of past visitors in
  order to explemefy state in clojure"
  [name]
  (swap! visitors conj name)
  (hello name))
