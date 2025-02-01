(ns auto-opti.prng.stateful
  "A stateful prng definition.

  Statefulness is important to ensure:
  * Each call to `rnd` is modifying the state of the prng.
  * it is thread safe to use."
  (:refer-clojure :exclude [rnd])
  (:require
   [cljc-long.core]))

(defprotocol PRNG
  (duplicate [_]
   "Duplicates this prng to a new one, starting at the seed value.")
  (jump [_]
   "Jump to a completly different place.")
  (uuid-seed [_]
   "Returns the uuid-seed of the random number generator.")
  (reset [_]
   "Returns a prng that starts again at the seed value.")
  (rnd-int [_ a b]
   "Returns a random integer between `a` and `b`")
  (rnd-double [_ a b]
   "Returns a random double between `a` and `b`"))

(defn as-ints
  "Draw `n` random integers with `prng`, between `[min-int; max-int[`."
  [prng n min-int max-int]
  (repeatedly n #(rnd-int prng min-int max-int)))
