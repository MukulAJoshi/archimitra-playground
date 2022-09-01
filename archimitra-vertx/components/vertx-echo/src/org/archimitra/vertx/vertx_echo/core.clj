(ns org.archimitra.vertx.vertx-echo.core
  (:import
   (io.vertx.core
    Vertx
    Handler)
   (io.vertx.core.net
    NetSocket))
  (:require
   [clojure.tools.logging :as log]
   [org.archimitra.vertx.util.core :as util]))

; Atom - number-of-connections - initial value set to 0
; Atom will enable thread safe update of number-of-connections
(def number-of-connections (atom 0))

(defn inc-connections []
  (swap! number-of-connections inc))

(defn dec-connections []
  (swap! number-of-connections dec))

(defn http-request-handler []
 (util/f-to-handler
   (fn [request]
     (.end (.response request) (str "We now have " @number-of-connections " connections")))))

(defn periodic-handler []
  (util/f-to-handler
    (fn [_]
      (str "We now have " @number-of-connections " connections"))))

(defn socket-handler [socket]
  (util/f-to-handler
    (fn [buffer]
      (log/info "In Socket Handler")
      (.write socket buffer)
      (if (.endsWith (.toString buffer) "/quit\n") 
        (.close socket)))))

(defn close-socket-handler []
 (util/f-to-handler
   (fn [_]
     (dec-connections))))

(defn net-connect-handler []
  (util/f-to-handler
    (fn [socket] 
      (log/info "In NetServer Connect Handler") 
      (inc-connections)
      (.handler socket (socket-handler socket)) 
      (.closeHandler socket (close-socket-handler)))))

; Core function that creates the Vertx NetServer and HttpServer 
(defn configure-vertx-server [^Vertx vertx]
  (doto (.createNetServer vertx)
    (.connectHandler (net-connect-handler))
    (.listen 3000))
  (.setPeriodic vertx 5000 (periodic-handler))
  (doto (.createHttpServer vertx)
    (.requestHandler (http-request-handler))
    (.listen 8080)))

(comment
  (try 
    (let [vertx (Vertx/vertx)] 
      (configure-vertx-server vertx)) 
    (catch Exception e (log/error "Woops" (.getMessage e)))))