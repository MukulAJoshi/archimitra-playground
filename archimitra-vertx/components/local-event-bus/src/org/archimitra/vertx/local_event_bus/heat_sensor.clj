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
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

;get the next random number
(defn delta []
  (let [next-int (.nextInt (ThreadLocalRandom/current))
        next-gaussian (.nextGaussian (ThreadLocalRandom/current))]
  (if (> next-int 0)
    next-gaussian
    (- next-gaussian))))

(defn update-message [^Vertx vertx timer-id]
  (let [current-context (Vertx/currentContext)
        sensor-id (.getLocal current-context "sensor-id")
        temperature ((fn [n] (+ n (/ (delta) 10))) (double 21.0)) 
        payload (assoc {} sensor-id temperature)
        event-bus (.eventBus vertx)]
    (log/info "publishing message : " sensor-id temperature)
    (.publish event-bus "sensor.updates" payload)
    (schedule-next-update vertx)))

(defn schedule-next-update [^Vertx vertx]
  (let [time-interval (+ (.nextInt (ThreadLocalRandom/current) 30000) 31000)]
    (log/info "time interval : " time-interval)
    (.setTimer vertx time-interval (reify Handler (handle [this timer-id]
                                                    (update-message vertx timer-id))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Heat Sensor Verticle instance using proxy
(defn ^Supplier create-heat-sensor-verticle []
  (util/f-to-supplier 
   #(proxy [AbstractVerticle] []
     (start [^Promise startPromise]
       (let [_ (log/info "BEFORE vertx and context in Heat Sensor Verticle")
             vertx (.getVertx this)
             _ (log/info "context : " (Vertx/currentContext))
             current-context (Vertx/currentContext)]
         (log/info (str "In START Heat Sensor Verticle with ID: " (.deploymentID this)))
         (.putLocal current-context "sensor-id" (.toString (UUID/randomUUID)))
         (schedule-next-update vertx)
         (.complete startPromise))))))