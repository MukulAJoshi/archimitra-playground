(ns org.archimitra.vertx.local-event-bus.core
  (:import
   (io.vertx.core
    Vertx
    DeploymentOptions
    Handler))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.local-event-bus.heat-sensor :as sensor :refer [create-heat-sensor-verticle]]
   [org.archimitra.vertx.local-event-bus.listener :as listener :refer [create-listener-verticle]]
   [org.archimitra.vertx.local-event-bus.sensor-data :as sensor-date :refer [create-sensor-data-verticle]]
   [org.archimitra.vertx.local-event-bus.http-server :as http-server :refer [create-http-server-verticle]]))

(defn handle-uncaught-exceptions [^Vertx vertx]
  (.exceptionHandler vertx (reify Handler 
                             (handle [this event] 
                               (log/error (str event " throws exception " (.getMessage event)))))))

(comment
  (try
    (let [vertx (Vertx/vertx)
          opts (doto (DeploymentOptions.)
                 (.setInstances 1)
                 (.setWorker true))
          sensor-opts (doto (DeploymentOptions.)
                        (.setInstances 4)
                        (.setWorker true))]
      (handle-uncaught-exceptions vertx)
      (log/info (.result (.deployVerticle vertx (create-heat-sensor-verticle) sensor-opts)))
      (log/info (.result (.deployVerticle vertx (create-listener-verticle) opts)))
      (log/info (.result (.deployVerticle vertx (create-sensor-data-verticle) opts)))
      (log/info (.result (.deployVerticle vertx (create-http-server-verticle) opts))))
    (catch Exception e (log/error "Woops" (.getMessage e)))))