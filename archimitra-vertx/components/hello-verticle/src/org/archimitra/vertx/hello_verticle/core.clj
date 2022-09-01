(ns org.archimitra.vertx.hello-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise)
   (io.vertx.core.http
    HttpServerRequest))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

; Atom - counter - initial value set to 1
; Atom will enable thread safe update of counter
(def counter (atom 1))

(defn tick-handler ^Handler []
  (util/f-to-handler
    (fn [_]
      (log/info "tick"))))

(defn request-handler ^Handler []
  (util/f-to-handler
    (fn [request]
      (log/info (str "Request #" (swap! counter inc) " from " (.host (.remoteAddress request))))
      (.end (.response request) "Hello!"))))

; Core function that creates the Vertx HttpServer 
(defn configure-vertx-server [^Vertx vertx]
  (.setPeriodic vertx 5000 (tick-handler))
  (doto (.createHttpServer vertx)
    (.requestHandler (request-handler))
    (.listen 8080))
  (log/info "open http://localhost:8080/"))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; refer: https://stackoverflow.com/questions/13134633/overriding-multi-arity-methods-in-proxy-in-clojure
; refer: https://kotka.de/blog/2010/03/proxy_gen-class_little_brother.html
; refer: https://github.com/eclipse-vertx/vert.x/blob/master/src/main/java/io/vertx/core/AbstractVerticle.java
(defn create-hello-verticle []
  (log/info "In Create Hello Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
      (configure-vertx-server (.getVertx this))
      (.complete startPromise))))

(comment
  (deref counter)
  (let [vertx (Vertx/vertx)]
    (try
      (log/info (.result (.deployVerticle vertx (create-hello-verticle))))
      (catch Exception e (log/error (.getMessage e))))))