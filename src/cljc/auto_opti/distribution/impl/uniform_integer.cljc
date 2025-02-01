(ns auto-opti.distribution.impl.uniform-integer
  "An uniform distribution of integers."
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.maths                              :as opt-maths]
   [auto-opti.prng.stateful                      :as opt-prng-stateful]))

(defrecord UniformInteger [prng a b]
  opt-distribution-prot/Distribution
    (draw [_] (opt-prng-stateful/rnd-int prng a b))
    (median [_] (/ (+ a b) 2))
    (cumulative [_ p]
      (cond
        (<= p a) 0.0
        (>= p b) 1.0
        :else (/ (- p a) (- b a))))
    (minimum [_] (reduce opt-maths/min [a b]))
    (maximum [_] (reduce opt-maths/max [a b]))
    (quantile [_ p]
      (cond
        (neg? p) a
        (= p 1.0) b
        :else (+ a (* p (- b a))))))

(defn make [prng a b] (->UniformInteger prng a b))
