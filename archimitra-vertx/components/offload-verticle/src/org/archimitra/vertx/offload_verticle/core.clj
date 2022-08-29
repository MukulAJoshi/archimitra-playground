(ns org.archimitra.vertx.offload-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Context
    Handler
    Verticle
    AbstractVerticle
    Promise
    AsyncResult
    DeploymentOptions)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]))

(defn blocking-code-handler []
  (reify Handler
    (handle [this promise]
      (log/info "Blocking code running")
      (try 
        (Thread/sleep 4000)
        (log/info "Done!")
        (.complete promise (str "Ok!"))
        (catch InterruptedException e (.fail promise e))))))

(defn result-handler []
  (reify Handler
    (handle [this async-result]
      (if (.succeeded async-result)
        (log/info (str "Blocking code result: " (.result async-result)))
        (log/error (str "Woops" (.cause async-result)))))))

(defn periodic-handler [^Vertx vertx]
  (reify Handler
    (handle [this _]
      (log/info "tick")
      (.executeBlocking vertx (blocking-code-handler) (result-handler)))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Worker Verticle instance using proxy
(defn ^Supplier create-offload-verticle []
  (util/f-to-supplier 
   #(proxy [AbstractVerticle] [] 
      (start [^Promise startPromise] 
        (log/info "In START") 
        (let [vertx (.getVertx this)] 
          (.setPeriodic vertx 5000 (periodic-handler vertx))) 
        (.complete startPromise)))))

(comment
  (let [vertx (Vertx/vertx)]
    (try
      (let [opts (doto (DeploymentOptions.)
                   (.setInstances 2)
                   (.setWorker true))]
        (log/info (.result (.deployVerticle vertx (create-offload-verticle) opts))))
      (catch Exception e (log/error "Woops" (.getMessage e))))))