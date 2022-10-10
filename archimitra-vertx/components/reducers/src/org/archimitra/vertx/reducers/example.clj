(ns org.archimitra.vertx.reducers.example
  (:require [clojure.string :as string]))

(def story ["The" "quick" "brown" "fox" "jumps" "over" "the" "lazy" "dog"])

(defn long-word? [^String word]
  (-> word
      .length
      (> 3)))

(defn logging [f]
  (fn [& args]
    (prn args)
    (apply f args)))

(defn step [result element]
  (if (long-word? element)
    (conj result (string/upper-case element))
    result))

;; transients

(defn step-1 [result element]
  (if (long-word? element)
    (conj! result (string/upper-case element))
    result))

;; abstract over step

(defn xf [step]
  (fn [result element]
    (if (long-word? element)
      (step result (string/upper-case element))
      result)))

;; filtering

(defn filtering [pred]
  (fn [step]
    (fn [result element]
      (if (pred element)
        (step result (string/upper-case element))
        result))))

;; mapping

(defn mapping [f]
  (fn [step]
    (fn [result element]
      (step result (f element)))))

(def xf-1
  (comp (filtering long-word?) (mapping string/upper-case)))

(comment
  (map string/upper-case (filter long-word? story))
  ;;
  (mapv string/upper-case (filter long-word? story))
  ;;
  (mapv string/upper-case (filterv long-word? story))
  ;;
  (reduce + 0 [1 2 4 8])
  ;;
  (+ (+ (+ (+ 0 1) 2) 4) 8)
  ;;
  (reduce conj [] story)
  ;;
  (conj (conj (conj (conj (conj (conj (conj (conj [] "The") "quick") "brown") "fox") "jumps") "over") "the") "lazy")
  ;;
  (reduce (logging conj) [] story)
  ;;
  (reduce step [] story)
  ;;
  (persistent! (reduce step-1 (transient []) story))
  ;;
  (reduce (xf conj) [] story)
  ;;
  (persistent! (reduce (xf conj!) (transient []) story))
  ;;
  (reduce ((filtering long-word?) conj) [] story)
  ;;
  (reduce ((mapping string/upper-case) conj) [] story)
  ;;
  ;; composition
  (reduce ((filtering long-word?) ((mapping string/upper-case) conj)) [] story)
  ;;
  '(f (g x))
  ;;
  '((comp f g ) x)
  ;;
  (reduce ((comp (filtering long-word?) (mapping string/upper-case)) conj) [] story)
  ;;
  (reduce (xf-1 conj) [] story))

