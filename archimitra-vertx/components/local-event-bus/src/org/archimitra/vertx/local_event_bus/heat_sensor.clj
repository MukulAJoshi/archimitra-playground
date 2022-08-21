(ns org.archimitra.vertx.local-event-bus.heat-sensor
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise)
   (java.util
    UUID)
   (java.util.concurrent
    ThreadLocalRandom)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]))

;sensor-id - to represent sensor id
(def sensor-id (.toString (UUID/randomUUID)))

;temperature - to represent the sensor temperature - since this is being updated, using atom
(def temperature (atom (double 21.0)))

;get the next random number
(defn delta []
  (let [next-int (.nextInt (ThreadLocalRandom/current))
        next-gaussian (.nextGaussian (ThreadLocalRandom/current))]
  (if (> next-int 0)
    next-gaussian
    (- next-gaussian))))

(defn update-message [^Vertx vertx timer-id]
  (let [temp (swap! temperature (fn [n] (+ n (/ (delta) 10)))) 
        payload (assoc {} "id" sensor-id
                          "temp" temp)
        event-bus (.eventBus vertx)]
    (log/info "publishing message : " sensor-id temp)
    (.publish event-bus "sensor.updates" payload)
    (schedule-next-update vertx)))

(defn schedule-next-update [^Vertx vertx]
  (let [time-interval (+ (.nextInt (ThreadLocalRandom/current) 5000) 1000)]
    (log/info "time interval : " time-interval)
    (.setTimer vertx time-interval (reify Handler (handle [this timer-id]
                                                    (update-message vertx timer-id))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Heat Sensor Verticle instance using proxy
(defn ^Supplier create-heat-sensor-verticle []
  (reify Supplier
    (get [this]
      (proxy [AbstractVerticle] []
        (start [^Promise startPromise]
          (log/info "In START Heat Sensor Verticle")
          (schedule-next-update (.getVertx this))
          (.complete startPromise))))))