(ns org.archimitra.rad.xtdb-adapter.core 
  (:import
   (java.io
    PushbackReader
    IOException))
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]))

#_ (defn read-config [path]
  (let [env (keyword (or (System/getenv "BIFF_ENV") "prod"))
        env->config (edn/read-string (slurp path))
        config-keys (concat (get-in env->config [env :merge]) [env])
        config (apply merge (map env->config config-keys))]
    config))

(defn read-config
  "Read the config.edn for XTDB connectivity"
  [^String source]
  (try 
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))
    (catch java.io.IOException e
      (log/error (.getMessage e)))
    (catch RuntimeException e
      (log/error (.getMessage e)))))
 

(comment
  ;
  (meta #'read-config)
  ;
  (clojure.repl/doc read-config)
  ;
  (:doc (meta #'read-config))
  ;
  (read-config "config.edn")
  ;
  )