(ns auto-opti.sample
  "Sampling is here to manage set of measures."
  (:require
   [auto-opti.sample.impl.vector :as opt-sample-vector]))

(defn make-vector [v] (opt-sample-vector/make v))

(defn sampling-rounded
  "For each multiple of `interval`, returns a map turning this multiple to its number of occurences"
  [vals interval]
  (->> vals
       (map #(* interval (int (/ % interval))))
       frequencies))

(defn sampling-raw
  "For each value of `interval`, returns a map turning this multiple to its number of occurences"
  [vals]
  (->> vals
       (map str)
       frequencies))
