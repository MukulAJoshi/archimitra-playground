(ns org.archimitra.vertx.vertx-echo.core
  (:import
   (io.vertx.core
    Vertx
    Handler)
   (io.vertx.core.net
    NetSocket)))

; Atom - number-of-connections - initial value set to 0
; Atom will enable thread safe update of number-of-connections
(def number-of-connections (atom 0))

(defn inc-connections []
  (swap! number-of-connections inc))

(defn dec-connections []
  (swap! number-of-connections dec))

; Atom - vertx - refers to the main entry point to Vertx functionality
(def vertx (atom (Vertx/vertx)))

; Core function that creates the Vertx NetServer and HttpServer 
(defn configure-vertx-server [^Vertx vertx]
  (doto (.createNetServer vertx)
    (.connectHandler (reify Handler
                       (handle [this socket]
                         ((println "In NetServer Connect Handler")
                          (inc-connections)
                          (.handler socket (reify Handler
                                             (handle [this buffer]
                                               ((println "In Socket Handler")
                                                (.write socket buffer)
                                                (if (.endsWith (.toString buffer) "/quit\n")
                                                  (.close socket))))))
                          (.closeHandler socket (reify Handler
                                                  (handle [this _]
                                                    (dec-connections))))))))
    (.listen 3000))
  (.setPeriodic vertx 5000
                (reify Handler
                  (handle [this _]
                    (str "We now have " @number-of-connections " connections"))))
  (doto (.createHttpServer vertx)
    (.requestHandler (reify Handler
                       (handle [this request]
                         (.end (.response request) (str "We now have " @number-of-connections " connections")))))
    (.listen 8080)))

(comment
  (deref number-of-connections)
  (deref vertx)
  (configure-vertx-server @vertx)
  )