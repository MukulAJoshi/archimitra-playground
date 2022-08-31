(ns org.archimitra.vertx.local-event-bus.sensor-data
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.json :as json]
   [org.archimitra.vertx.util.interface :as util]))

;last-values - atom to represent last values map
(def last-values (atom {}))

;update-message-handler - handle message from subscription
(defn update-message-handler []
  (reify Handler
    (handle [this message]
      (let [message-body (.body message)
            id (key (first message-body))
            temperature (val (first message-body))]
        (log/info (str "id is string? " (string? id) id))
        (log/info (str "temperature is string? " (string? temperature) temperature))
        (swap! last-values assoc id temperature)))))

;update-message-handler - handle message from subscription
(defn average-message-handler []
  (reify Handler
    (handle [this message]
      (try
        (let [temperature-values (vals @last-values)
              _ (log/info @last-values)
              _ (log/info temperature-values)
              temperature-average (/ (reduce + temperature-values) (count temperature-values))]
          (log/info "JSON string : " (json/write-str (assoc {} "average" temperature-average)))
          (.reply message (json/write-str (assoc {} "average" temperature-average)))
          (log/info "temperature values and average : " temperature-values temperature-average))
      (catch Exception e (log/error "Oops" (.getMessage e)))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Sensor Data Verticle instance using proxy
(defn ^Supplier create-sensor-data-verticle []
  (util/f-to-supplier
   #(proxy [AbstractVerticle] []
     (start [^Promise startPromise]
       (let [event-bus (.eventBus (.getVertx this))]
         (log/info "In START Sensor Data Verticle")
         (.consumer event-bus
                    "sensor.updates"
                    (update-message-handler))
         (.consumer event-bus
                    "sensor.average"
                    (average-message-handler))
         (.complete startPromise))))))