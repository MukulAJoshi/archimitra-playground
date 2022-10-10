(ns org.archimitra.vertx.dependency-injection.core
  (:require [donut.system :as ds]))

(def Component
  {::ds/start "start component"})

(comment
  ::ds/start
  ;;
  :donut.system{:start "start component"}
  ;;
  #::ds{:start "start component"}
  ;;
  #::ds{:start "start component"
        :stop "stop component"}
  ;;
  (let [{:keys [::ds/start]} Component]
    (prn "start is: " start)))