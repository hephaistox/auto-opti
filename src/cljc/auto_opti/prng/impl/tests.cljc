(ns auto-opti.prng.impl.tests
  "Prng tests."
  (:require
   [auto-opti.sample.impl.number-set :as opt-number-set]))

(defn distribution-uniformity
  "If the coefficient of variation (i.e. `cv`) is lower than `max-cv`, returns nil.
  Otherwise, return a map with the coefficient of variation of the collection."
  [coll max-cv]
  (let [freq (frequencies coll)
        std-dev (opt-number-set/standard-deviation (vals freq))
        avg (opt-number-set/average (vals freq))
        cv (* 100.0 (/ std-dev avg))]
    (when (>= cv max-cv)
      {:std-dev std-dev
       :freq freq
       :avg avg
       :max-cv max-cv
       :cv (* 100.0 (/ std-dev avg))})))
