(ns org.archimitra.vertx.reducers.core)

(defn map [f coll]
  (when (not= '() coll)
    (conj 
      (map f (rest coll))
      (f (first coll)))))

(defn filter [pred coll]
  (when (not= '() coll)
    (let [f (first coll) r (rest coll)]
      (if (pred f)
        (conj (filter pred r) f)
        (filter pred r)))))

(defn reduce [f result coll]
  (if (not= '() coll)
    (reduce f (f result (first coll)) (rest coll))
    result))

(defn map-rf [f result coll]
  (if (not= '() coll)
    (map-rf f (f result (first coll)) (rest coll))
    result)); This is Reduce - just replace map-rf with reduce

(defn filter-rf [f result coll]
  (if (not= '() coll)
    (filter-rf f (f result (first coll)) (rest coll))
    result)); This is Reduce - just replace filter-rf with reduce

(defn map-rf-1 [rf]
  (fn [result el]
    (rf result (inc el))))

(defn filter-rf-1 [rf]
  (fn [result el]
    (if (odd? el)
      (rf result el)
      result)))

(defn map-rf-2 [f]
  (fn [rf]
    (fn [result el]; this is the final function that is exposed to the reduce - takes accumulation and element
      (rf result (f el)))))

(defn filter-rf-2 [pred?]
  (fn [rf]
    (fn [result el]; this is the final function that is exposed to the reduce - takes accumulation and element
      (if (pred? el)
        (rf result el)
        result))))

(comment
  (reduce + 0 (range 10))
  ;;
  (map-rf (fn [result el]
            (conj result (inc el))) [] (range 10))
  ;;
  (filter-rf (fn [result el]
               (if (odd? el) 
                 (conj result el)
                 result)) [] (range 10))
  ;;
  (reduce ((map-rf-2 inc) conj) [] (range 10))
  ;;
  (reduce ((filter-rf-2 odd?) conj) [] (range 10))
  )