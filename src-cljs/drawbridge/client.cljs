(ns drawbridge.client
  (:require [goog.events :as events]
            [goog.net.XhrIo]
            [goog.Uri.QueryData]))

(defn make-js-map
  "makes a javascript map from a clojure one"
  [cljmap]
  (let [out (js-obj)]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn make-clj-map
  [j]
  (reduce #(assoc %1 %2 (aget j %2)) {} (js-keys j)))

(def polling-nrepl? (atom false))

(def nrepls (atom {}))

(defn nrepl-data-handler [data status xhr]
  (doseq [item (seq data)]
    (if-let [result (get @nrepls (aget item "id"))]
      (let [oldv @(.-results result)]
        (swap! (.-results result) conj (make-clj-map item))
        (-notify-watches result oldv @(.-results result))))))

(deftype NreplResult [results id watchers url]
  IDeref
  (deref [repl]
    results)
  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[k v] @watchers]
      (try
        (v k this oldval newval)
        (catch js/Object foo
          (.log js/console foo)))))
  (-add-watch [this key f]
    (swap! watchers assoc key f))
  (-remove-watch [this key] 
    (swap! watchers dissoc key)))

(defn nrepl-close [result]
  (swap! nrepls dissoc (.-id result)))

(def nrepl-polling-timeout (atom 1000))

(defn poll-nrepl []
  (when-not @polling-nrepl?
    (reset! polling-nrepl? true)
    (js/setInterval
     (fn []
       (when-not (empty? @nrepls)
         (doseq [url (distinct (map (fn [x] (.-url x)) (vals @nrepls)))]
           (let [request (goog.net.XhrIo.)]
             (events/listen request "complete"
                            (fn []
                              (if (.isSuccess request)
                                (nrepl-data-handler
                                 (.getResponseJson request)
                                 "success"
                                 request)
                                (js/alert "nrepl error"))))
             (.send request url "GET"
                    (make-js-map
                     {"REPL-Response-Timeout" @nrepl-polling-timeout}))))))
     2000)))

(defn nrepl-send [url data]
  (poll-nrepl)
  (let [id (name (gensym 'id))
        result (NreplResult. (atom []) id (atom {}) url)
        request (goog.net.XhrIo.)]
    (swap! nrepls assoc id result)
    (events/listen request "complete"
                   (fn []
                     (if (.isSuccess request)
                       (nrepl-data-handler
                        (.getResponseJson request)
                        "success"
                        request)
                       (js/alert "nrepl error"))))
    (.send request url "POST"
           (.toString
            (reduce
             (fn [form [k v]]
               (doto form (.add (name k) v)))
             (doto (goog.Uri.QueryData.)
               (.add "id" id))
             data))
           (make-js-map
            {"REPL-Response-Timeout" (/ @nrepl-polling-timeout 2)}))
    result))
