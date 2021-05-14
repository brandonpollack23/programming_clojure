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

;; Agents
(def counter (agent 0 :validator number?))
;; send to send to shared thread pool, send-off for it's own thread (blocking stuff).
(send counter inc) ;; Executes on another thread and returns immediately.
(await-for 1000 counter) ;; wait for agent to be fully up to date.
(send counter (fn [_] "foo")) ;; This will fail due to the vamidator
(agent-error counter) ;; Get the error
(restart-agent counter 0) ;; start it over

;; we can have handlers, though
(defn handler [agent err]
  (println "ERR!" (.getMessage err)))
(def counter2 (agent 0 :validator number? :error-handler handler))
(send counter2 (fn [_] "foo")) ;; This will fail due to the vamidator, but now is "handled" so the agent continues
(send counter2 inc)
(send counter2 inc)
(await-for 1000 counter)

;; Agents can coordinate with transactions on refs, this is useful to execute a
;; side effect only when the dosync transaction succeeds.
;; This is because any "send" calls in a dosync block will only send after the
;; transaction succeeds.
(def backup-agent (agent "output/messages-backup.clj"))
(defn add-mesage-with-backup [msg]
  (dosync
   (let [snapshot (commute messages conj msg)]
     ;; send-off because writes are blocking calls
     (send-off backup-agent (fn [filename]
                              (spit filename snapshot)
                              filename))
     snapshot)))
