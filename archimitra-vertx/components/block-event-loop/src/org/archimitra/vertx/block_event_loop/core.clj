(ns org.archimitra.vertx.block-event-loop.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

(defn timer-handler ^Handler []
  (util/f-to-handler
    (fn [_] 
      (while true ()))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
(defn create-block-event-loop-verticle []
  (log/info "In Create Block Event Loop Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
      (log/info "In START")
      (.setTimer (.getVertx this) 1000 (timer-handler))
      (.complete startPromise))))

(comment
  (try
    (let [vertx (Vertx/vertx)] 
      (log/info (.result (.deployVerticle vertx (create-block-event-loop-verticle))))) 
    (catch Exception e (log/error (.getMessage e)))))