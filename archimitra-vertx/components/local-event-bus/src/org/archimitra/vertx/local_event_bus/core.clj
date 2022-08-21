(ns org.archimitra.vertx.local-event-bus.core
  (:import
   (io.vertx.core
    Vertx
    DeploymentOptions))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.local-event-bus.heat-sensor :as sensor :refer [create-heat-sensor-verticle]]
   [org.archimitra.vertx.local-event-bus.listener :as listener :refer [create-listener-verticle]]))

; Atom - vertx - refers to the main entry point to Vertx functionality
(def vertx (atom (Vertx/vertx)))

(comment
  (deref vertx)
  (log/info @vertx)
  (try
    (let [opts (doto (DeploymentOptions.)
                 (.setInstances 1)
                 (.setWorker true))]
      (log/info (.result (.deployVerticle @vertx (create-heat-sensor-verticle) opts)))
      (log/info (.result (.deployVerticle @vertx (create-listener-verticle) opts))))
    (catch Exception e (log/error "Woops" (.getMessage e)))))