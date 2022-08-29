(ns org.archimitra.vertx.util.core
  (:import
   (java.util.function
    Supplier)))

(defn ^Supplier f-to-supplier [f]
  "Converts a function to java.util.function.Supplier."
  (reify Supplier
    (get [this] (f))))