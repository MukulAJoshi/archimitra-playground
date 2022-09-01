(ns org.archimitra.vertx.util.core
  (:import
   (java.util.function
    Supplier)
   (io.vertx.core
    Handler)))

(defn ^Supplier f-to-supplier
  "Converts a function to java.util.function.Supplier."
  [f]
  (reify Supplier
    (get [this] (f))))

(defn ^Handler f-to-handler
  "Converts a function to io.vertx.core.Handler"
  [f]
  (reify Handler
    (handle [this arg] (f arg))))