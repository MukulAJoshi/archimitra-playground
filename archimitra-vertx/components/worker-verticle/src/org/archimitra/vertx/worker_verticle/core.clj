(ns org.archimitra.vertx.worker-verticle.core
  (:import
   (io.vertx.core
    Vertx
    Context
    Handler
    Verticle
    AbstractVerticle
    Promise
    DeploymentOptions)
   (java.util.function
    Supplier))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.interface :as util]
   [donut.system :as ds]))

(defn periodic-handler []
  (util/f-to-handler
    (fn [_] 
      (try 
        (log/info "Zzzz..")
        (Thread/sleep 8000)
        (log/info "Up") 
        (catch InterruptedException e (log/error (.getMessage e)))))))

; proxied methods cannot be invoked from the abstract class
; thus need to override the base method in the abstract class
; so the proxy will override the start(Promise<Void>) which is the base method
; Supplier Functional Interface is used to get the Worker Verticle instance using proxy
(defn ^Supplier create-worker-verticle [] 
  (util/f-to-supplier 
   #(proxy [AbstractVerticle] []
     (start [^Promise startPromise]
       (log/info "In START")
       (.setPeriodic (.getVertx this) 10000 (periodic-handler))
       (.complete startPromise)))))

; create the donut system definition
(def system
  {::ds/defs
   {:vertx {:service #::ds{:start (fn [{{:keys [options]} ::ds/config}]
                                    (Vertx/vertx))
                           :stop (fn [{::ds/keys [instance]}]
                                   (.close instance))
                           :config {:options {}}}}
    :worker-verticle {:service #::ds{:start (fn [{{:keys [vertx options supplier]} ::ds/config}]
                                             (try
                                               (.result (.deployVerticle vertx supplier options))
                                               (catch Exception e (log/error (.getMessage e)))))
                                     :stop (fn [{{:keys [vertx]} ::ds/config 
                                                 ::ds/keys [instance]}] 
                                             (try 
                                               (.result (.undeploy vertx instance)) 
                                               (catch Exception e (log/error (.getMessage e)))))
                                     :config {:vertx (ds/ref [:vertx :service]) 
                                              :options (doto (DeploymentOptions.) 
                                                         (.setInstances 2) 
                                                         (.setWorker true)) 
                                              :supplier (ds/local-ref [:supplier])}}
                      :supplier (util/f-to-supplier 
                                 #(proxy [AbstractVerticle] [] 
                                    (start [^Promise startPromise] 
                                      (log/info "In START") 
                                      (.setPeriodics (.getVertx this) 10000 (util/f-to-handler
                                                                             (fn [_]
                                                                               (try
                                                                                 (log/info "Zzzz..")
                                                                                 (Thread/sleep 5000)
                                                                                 (log/info "Up")
                                                                                 (catch InterruptedException e (log/error (.getMessage e))))))) 
                                      (.complete startPromise))))}}})

(comment
  (try
    (let [vertx (Vertx/vertx)
          opts (doto (DeploymentOptions.)
                 (.setInstances 2)
                 (.setWorker true))]
      (log/info (.result (.deployVerticle vertx (create-worker-verticle) opts))))
    (catch Exception e (log/error "Woops" (.getMessage e))))
    ;;
  (let [running-system (ds/signal system ::ds/start)]
    (Thread/sleep 50000)
    (ds/signal running-system ::ds/stop))
  ;;
  (ds/signal system ::ds/start))