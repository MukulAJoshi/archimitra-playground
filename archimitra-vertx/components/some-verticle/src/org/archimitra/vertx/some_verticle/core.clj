(ns org.archimitra.vertx.some-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]))

; Atom - counter - initial value set to 1
; Atom will enable thread safe update of counter
(def counter (atom 1))

; Atom - vertx - refers to the main entry point to Vertx functionality
(def vertx (atom (Vertx/vertx)))

; Core function that creates the Vertx HttpServer
; Check the async result of starting the HttpServer and then complete/fail the Promise
(defn configure-vertx-server [^Vertx vertx ^Promise startPromise]
  (log/info "In Config Vertx")
  (log/info vertx)
  (.setPeriodic vertx 5000
                (reify Handler
                  (handle [this _]
                    (log/info "tick"))))
  (doto (.createHttpServer vertx)
    (.requestHandler (reify Handler
                       (handle [this request]
                         ((log/info "Request #{} from {}" (swap! counter inc) (.host (.remoteAddress request)))
                          (.end (.response request) "Hello!")))))
    (.listen 8080 (reify Handler
                    (handle [this async-result]
                      (if (.succeeded async-result) 
                        (.complete startPromise) 
                        (.fail startPromise (.cause async-result)))))))
  (log/info "open http://localhost:8080/"))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
(defn create-hello-verticle []
  (log/info "In Create Hello Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
           (log/info "In START")
           (configure-vertx-server (.getVertx this) startPromise))))

(comment
  (deref counter)
  (deref vertx)
  (log/info @vertx)
  (try
    (log/info (.result (.deployVerticle @vertx (create-hello-verticle))))
    (catch Exception e (log/error (.getMessage e)))))