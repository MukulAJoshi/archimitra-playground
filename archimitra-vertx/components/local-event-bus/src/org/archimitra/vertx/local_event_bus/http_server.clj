(ns org.archimitra.vertx.local-event-bus.http-server
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise
    TimeoutStream
    AsyncResult)
   (io.vertx.core.eventbus
    EventBus
    MessageConsumer)
   (io.vertx.core.http
    HttpServerRequest
    HttpServerResponse)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]
   [clojure.data.json :as json]
   [org.archimitra.vertx.util.interface :as util]))

(defn reply-handler ^Handler [^HttpServerResponse response]
  (util/f-to-handler
    (fn [async-result]
      (if (.succeeded async-result)
        (let [result (.body (.result async-result))
              _ (log/info "Average : " result)]
          (.write response "event: average\n")
          (.write response (str "data: " result "\n\n")))))))

(defn ticks-handler ^Handler [^EventBus event-bus ^HttpServerResponse response]
  (util/f-to-handler
    (fn [_]
      (.request event-bus "sensor.average" "" (reply-handler response)))))

(defn message-handler ^Handler [^HttpServerResponse response]
  (util/f-to-handler
    (fn [message]
      (let [message-body (.body message)]
        (doto response
          (.write "event: update\n")
          (.write (str "data : " (json/write-str message-body) "\n\n")))))))

(defn sse [^HttpServerResponse response ^Vertx vertx]
  (let [event-bus (.eventBus vertx)
        message-consumer (.consumer event-bus "sensor-updates")
        ticks (.periodicStream vertx 1000)]
    (doto response
      (.putHeader "Content-Type" "text/event-stream")
      (.putHeader "Cache-Control" "no-cache")
      (.setChunked true))
    (.handler message-consumer (message-handler response))
    (.handler ticks (ticks-handler event-bus response))
    (.endHandler response (reify Handler
                            (handle [this _]
                              (.unregister message-consumer)
                              (.cancel ticks))))))

(defn request-handler ^Handler [^Vertx vertx]
  (util/f-to-handler
    (fn [request]
      (let [path (.path request)
            response (.response request)]
        (case path
          "/" (.sendFile response "index.html")
          "/sse" (sse response vertx)
          (.setStatusCode response 404))))))

(defn listener-handler ^Handler [^Promise startPromise]
  (util/f-to-handler
    (fn [async-result]
      (if (.succeeded async-result)
        (.complete startPromise)
        (.fail startPromise (.cause async-result))))))

; Core function that creates the Vertx HttpServer
; Check the async result of starting the HttpServer and then complete/fail the Promise
(defn configure-vertx-server [^Vertx vertx ^Promise startPromise]
  (log/info "In Config Vertx")
  (log/info vertx)
  (doto (.createHttpServer vertx)
    (.requestHandler (request-handler vertx))
    (.listen 8080 (listener-handler startPromise))
  (log/info "open http://localhost:8080/")))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
(defn create-http-server-verticle ^Supplier []
  (util/f-to-supplier 
   #(proxy [AbstractVerticle] []
     (start [^Promise startPromise]
       (log/info "In START Http Server")
       (configure-vertx-server (.getVertx this) startPromise)))))