(ns auto-opti.proba-dist.impl.normal
  "Normal distribution."
  {:no-doc true}
  (:require
   [auto-opti.maths                            :as opt-maths]
   [auto-opti.maths.gamma                      :as opt-maths-gamma]
   [auto-opti.prng                             :as opt-prng]
   [auto-opti.prng.stateful                    :as opt-prng-stateful]
   [auto-opti.proba-dist.distribution-protocol :as opt-dstb-prot]))

(defn rand-normal
  [prng]
  (* (opt-maths/sqrt (* -2 (opt-maths/log (opt-prng-stateful/rnd-double prng 0 1))))
     (opt-maths/cos (* 2 opt-maths/PI (opt-prng-stateful/rnd-double (opt-prng/jump prng) 0 1)))))

(defrecord Normal [prng mu sd]
  opt-dstb-prot/Distribution
    (draw [_] (+ (* (rand-normal prng) sd) mu))
    (median [_] mu)
    (cumulative [_ p] (* 0.5 (+ 1 (opt-maths-gamma/erf (/ (- p mu) (opt-maths/sqrt (* 2 sd sd)))))))
    (minimum [_] opt-maths/negative-infinity)
    (maximum [_] opt-maths/infinity)
    (quantile [_ p] (+ (* -1.41421356237309505 sd (opt-maths-gamma/erfcinv (* 2 p))) mu)))

(defn make
  "Generates an exponential distribution based on `prng` with rate `rate`."
  [prng mu sd]
  (->Normal prng mu sd))

(defrecord NormalInteger [prng mu sd]
  opt-dstb-prot/Distribution
    (draw [_] (int (+ (* (rand-normal prng) sd) mu)))
    (median [_] mu)
    (cumulative [_ p] (* 0.5 (+ 1 (opt-maths-gamma/erf (/ (- p mu) (opt-maths/sqrt (* 2 sd sd)))))))
    (minimum [_] opt-maths/negative-infinity)
    (maximum [_] opt-maths/infinity)
    (quantile [_ p] (int (+ (* -1.41421356237309505 sd (opt-maths-gamma/erfcinv (* 2 p))) mu))))

(defn make-integer [prng mu sd] (->NormalInteger prng mu sd))
