(ns org.archimitra.vertx.local-event-bus.listener
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise)
   (java.text
    DecimalFormat)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

;decimal-format - to represent decimal format
(def decimal-format (DecimalFormat. "#.##"))

;message-handler - handle message from subscription
(defn message-handler ^Handler []
  (util/f-to-handler
    (fn [message]
      (let [message-body (.body message)
            id (key (first message-body))
            temperature (.format decimal-format (val (first message-body)))]
        (log/info (str id " reports a temperature ~" temperature "C"))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Listener Verticle instance using proxy
(defn create-listener-verticle ^Supplier []
  (util/f-to-supplier
   #(proxy [AbstractVerticle] []
     (start [^Promise startPromise]
       (let [event-bus (.eventBus (.getVertx this))]
         (log/info "In START Listener Verticle")
         (.consumer event-bus
                    "sensor.updates"
                    (message-handler))
         (.complete startPromise))))))