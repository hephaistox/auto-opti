(ns auto-opti.prng.stateful
  "A stateful prng definition.

  Statefulness ensures each call to a `rnd-*` method advances the state of the prng.

  Thread-safety is IMPLEMENTATION-DEPENDENT and not guaranteed by this protocol:
  * `:xoroshiro128` (the portable default, backed by the stateful wrapper's atom)
    is thread-safe.
  * the fast variants `:xoroshiro128-jvm`, `:xoroshiro256-jvm` and
    `:xoroshiro128-js` mutate their state in place and are NOT thread-safe; give
    each thread its own generator with `duplicate` (or `jump`)."
  {:no-doc true}
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
