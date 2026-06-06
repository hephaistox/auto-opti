(ns auto-opti.proba-dist.impl.uniform
  "An uniform distribution, returning a double between `a` and `b`."
  {:no-doc true}
  (:require
   [auto-opti.maths                            :as opt-maths]
   [auto-opti.prng.stateful                    :as opt-prng-stateful]
   [auto-opti.proba-dist.distribution-protocol :as opt-distribution-prot]))

(defrecord Uniform [prng a b width]
  opt-distribution-prot/Distribution
    (draw [_] (opt-prng-stateful/rnd-double prng a b))
    (median [_] (/ (+ a b) 2.0))
    (cumulative [_ p]
      (cond
        (<= p a) 0.0
        (>= p b) 1.0
        :else (/ (- p a) width)))
    (minimum [_] (reduce opt-maths/min [a b]))
    (maximum [_] (reduce opt-maths/max [a b]))
    (quantile [_ p]
      (cond
        (zero? p) a
        (= p 1.0) b
        :else (+ a (* p width)))))

(defn make [prng a b] (when (every? number? [a b]) (->Uniform prng a b (double (- b a)))))
