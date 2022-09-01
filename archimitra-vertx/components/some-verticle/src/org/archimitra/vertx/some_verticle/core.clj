(ns org.archimitra.vertx.some-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

; Atom - counter - initial value set to 1
; Atom will enable thread safe update of counter
(def counter (atom 1))

(defn periodic-handler ^Handler []
  (util/f-to-handler
    (fn [_]
      (log/info "tick"))))

(defn request-handler ^Handler []
  (util/f-to-handler
    (fn [request] 
      (log/info (str "Request #" (swap! counter inc) " from " (.host (.remoteAddress request))))
      (.end (.response request) "Hello!"))))

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
  (.setPeriodic vertx 5000 (periodic-handler))
  (doto (.createHttpServer vertx)
    (.requestHandler (request-handler))
    (.listen 8080 (listener-handler startPromise)))
  (log/info "open http://localhost:8080/"))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
(defn create-some-verticle []
  (log/info "In Create Some Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise] 
      (log/info "In START") 
      (configure-vertx-server (.getVertx this) startPromise))))

(comment
  (try
    (let [vertx (Vertx/vertx)] 
      (log/info (.result (.deployVerticle vertx (create-some-verticle))))) 
    (catch Exception e (log/error (.getMessage e)))))