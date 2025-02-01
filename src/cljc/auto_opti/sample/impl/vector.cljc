(ns auto-opti.sample.impl.vector
  "A sample data stored in memory - a vector."
  (:require
   [auto-opti.sample.impl.number-set :as opt-number-set]
   [auto-opti.sample.sample-protocol :as opt-sample-protocol]))

(defrecord SampleVector [v]
  opt-sample-protocol/SampleData
    (average [_] (opt-number-set/average v))
    (n [_] (count v))
    (median [_] (opt-number-set/median v))
    (variance [_] (opt-number-set/variance v)))

(defn make "Returns sample `v`." [v] (->SampleVector v))
