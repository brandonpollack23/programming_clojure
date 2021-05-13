(ns chapter6.core)

(defn rand-char
  []
  (char
   ((rand-nth
     [#(+ (rand 26) (int \A))
      #(+ (rand 26) (int \a))]))))

(defn rand-str [len]
  (->> rand-char
       (repeatedly len)
       (apply str)))

;; STM

(def current-track (ref "Mars, the Bringer of War"))
(def current-composer (ref "Holst"))

(defn change-song [] (dosync
                      (let [strfn (rand-str (+ 3 (rand 5)))]
                        (ref-set current-track strfn)
                        (ref-set current-composer strfn))))

(defn access-album-info []
  (dosync
   [@current-track @current-composer]))

(defn- print-and-update-album
  []
  (change-song)
  (let [album-info (access-album-info)]
    (locking *out* (println album-info))))

(apply pcalls (repeat 10 print-and-update-album))

;; Simple chat


(defrecord Message [sender text])
(defn valid-message? [msg]
  (and (:sender msg) (:text msg)))
(def validate-message-list #(every? valid-message? %))

(def messages (ref () :validator validate-message-list))
(defn add-message [msg]
  (dosync (alter messages conj msg)))

;; This is great but lots of time things don't need to be coordinated (uh like that chat app?)
