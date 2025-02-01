(ns auto-opti.prng.stateful-wrapper
  "Creates a stateful prng based on a stateless one."
  (:require
   [auto-opti.prng.stateful  :as opt-prng-stateful]
   [auto-opti.prng.stateless :as opt-prng-stateless]))

(declare make)

(defrecord PrngStatefulImpl [stateless-prng original-prng]
  opt-prng-stateful/PRNG
    (duplicate [_] (make original-prng))
    (jump [this] (do (swap! stateless-prng opt-prng-stateless/jump) this))
    (uuid-seed [_] (opt-prng-stateless/uuid-seed @stateless-prng))
    (reset [this] (do (reset! stateless-prng original-prng) this))
    (rnd-int [_ a b]
      (let [val (opt-prng-stateless/peek-int @stateless-prng a b)]
        (swap! stateless-prng opt-prng-stateless/next)
        val))
    (rnd-double [_ a b]
      (let [val (opt-prng-stateless/peek-double @stateless-prng a b)]
        (swap! stateless-prng opt-prng-stateless/next)
        val)))

(defn make [stateless-prng] (->PrngStatefulImpl (atom stateless-prng) stateless-prng))
