(ns org.archimitra.vertx.block-event-loop.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]))

; Atom - vertx - refers to the main entry point to Vertx functionality
(def vertx (atom (Vertx/vertx)))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; refer: https://stackoverflow.com/questions/13134633/overriding-multi-arity-methods-in-proxy-in-clojure
; refer: https://kotka.de/blog/2010/03/proxy_gen-class_little_brother.html
; refer: https://github.com/eclipse-vertx/vert.x/blob/master/src/main/java/io/vertx/core/AbstractVerticle.java
(defn create-block-event-loop-verticle []
  (log/info "In Create Block Event Loop Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
      (log/info "In START")
      (.setTimer (.getVertx this) 
                   1000 
                   (reify Handler
                     (handle [this _]
                       (while true ()))))
      (.complete startPromise))))

(comment
  (deref vertx)
  (log/info @vertx)
  (try
    (log/info (.result (.deployVerticle @vertx (create-block-event-loop-verticle))))
    (catch Exception e (log/error (.getMessage e)))))