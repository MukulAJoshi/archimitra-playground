(ns org.archimitra.vertx.util.interface 
  (:import
   (java.util.function
    Supplier))
  (:require
   [org.archimitra.vertx.util.core :as core]))

(defn ^Supplier f-to-supplier [f]
  "Converts a function to java.util.function.Supplier."
  (core/f-to-supplier f))