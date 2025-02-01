(ns auto-opti.prng.impl.built-in
  "PRNG implementation built-in in your platform."
  (:refer-clojure :exclude [next])
  (:require
   [auto-opti.prng.stateful :as opt-prng-stateful]))

(defrecord Builtin []
  opt-prng-stateful/PRNG
    (duplicate [_] (throw (ex-info "Not implemented." {})))
    (jump [_] (repeatedly 100 rand))
    (reset [_]
      (throw
       (ex-info
        "Not implemented. Leverage another prng or implement https://github.com/trystan/random-seed"
        {})))
    (uuid-seed [_] nil)
    (rnd-int [_ a b] (+ a (mod (rand) (- b a))))
    (rnd-double [_ a b] (+ a (mod (rand) (- b a)))))

(defn make [] (->Builtin))
