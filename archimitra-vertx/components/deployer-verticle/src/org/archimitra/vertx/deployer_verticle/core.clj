(ns org.archimitra.vertx.deployer-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Handler
    AbstractVerticle
    Promise))
  (:require
   [clojure.tools.logging :as log]))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
(defn create-empty-verticle []
  (log/info "In Create Empty Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
      (log/info "In Empty Verticle START")
      (.complete startPromise))
    (stop [^Promise stopPromise]
      (log/info "In Empty Verticle STOP")
      (.complete stopPromise))))

(defn undeploy-handler [id]
  (reify Handler
    (handle [this async-result]
      (if (.succeeded async-result)
        (log/info "{} was undeployed" id)
        (log/error "{} could not be deployed" id)))))

; undeploy - verticle represented by id
(defn undeploy-later [^Vertx vertx id]
  (.undeploy vertx id (undeploy-handler id)))

(defn deploy-handler [^Vertx vertx ^Promise startPromise]
  (reify Handler
    (handle [this async-result]
      (if (.succeeded async-result)
        (let [id (.result async-result)]
          (.complete startPromise)
          (log/info "Successfully deployed {}" id)
          (.setTimer vertx 5000 (reify Handler
                                  (handle [this timer-id]
                                    (undeploy-later vertx id)))))
        ((let [cause (.cause async-result)]
           (log/error "Error while deploying" cause)
           (.fail startPromise cause)))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Deploy Verticle is used to deploy the Empty Verticle
(defn create-deploy-verticle []
  (log/info "In Create Deploy Verticle")
  (proxy [AbstractVerticle] []
    (start [^Promise startPromise]
      (let [delay 1000
            vertx (.getVertx this)]
        (log/info "In Deploy Verticle START")
        (.deployVerticle vertx (create-empty-verticle) (deploy-handler vertx startPromise))))))

(comment
  (let [vertx (Vertx/vertx)]
    (try 
      (log/info (.result (.deployVerticle vertx (create-deploy-verticle))))
      (catch Exception e (log/error (.getMessage e))))))