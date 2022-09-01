(ns org.archimitra.vertx.util.interface 
  (:import
   (java.util.function
    Supplier)
   (io.vertx.core
    Handler))
  (:require
   [org.archimitra.vertx.util.core :as core]))

(defn ^Supplier f-to-supplier
  "Converts a function to java.util.function.Supplier."
  [f]
  (core/f-to-supplier f))

(defn ^Handler f-to-handler
  "Converts a function to io.vertx.core.Handler"
  [f]
  (core/f-to-handler f))