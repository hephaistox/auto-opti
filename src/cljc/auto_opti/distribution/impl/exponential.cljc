(ns auto-opti.distribution.impl.exponential
  "Exponential distribution based on [inversion method](https://en.wikipedia.org/wiki/Inverse_transform_sampling).

  See the [wiki article](https://en.wikipedia.org/wiki/Exponential_distribution)."
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.maths                              :as opt-maths]
   [auto-opti.prng.stateful                      :as opt-prng-stateful]))

(defrecord Exponential [prng rate]
  opt-distribution-prot/Distribution
    (draw [_] (/ (- (opt-maths/log (opt-prng-stateful/rnd-double prng 0.0 1.0))) rate))
    (median [_] (/ (- (opt-maths/log 0.5)) rate))
    (cumulative [_ p] (- 1.0 (opt-maths/exp (- (* rate p)))))
    (minimum [_] 0)
    (maximum [_] opt-maths/infinity)
    (quantile [_ x] (/ (- (opt-maths/log (- 1.0 x))) rate)))

(defn make
  "Generates an exponential distribution based on `prng` with rate `rate`."
  [prng rate]
  (->Exponential prng rate))
