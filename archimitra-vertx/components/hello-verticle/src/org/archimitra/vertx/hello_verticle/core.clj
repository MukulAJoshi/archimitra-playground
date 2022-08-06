(ns org.archimitra.vertx.hello-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]))

; Atom - number-of-connections - initial value set to 0
; Atom will enable thread safe update of number-of-connections
(def counter (atom 1))

; Atom - vertx - refers to the main entry point to Vertx functionality
(def vertx (atom (Vertx/vertx)))

; Core function that creates the Vertx HttpServer 
(defn configure-vertx-server [^Vertx vertx]
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
      (log/info "In START")
      (configure-vertx-server (.getVertx this))
      (.complete startPromise))))

(comment
  (deref counter)
  (deref vertx)
  (log/info @vertx)
  (try 
    (log/info (.result (.deployVerticle @vertx (create-hello-verticle))))
    (catch Exception e (log/error (.getMessage e))))
  )