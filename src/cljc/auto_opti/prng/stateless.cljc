(ns auto-opti.prng.stateless
  "Implements PRNG without state.

  Use this protocol to wrap some stateless implementations of a `prng`.
  To use prng implementing protocol, wraps it again with `PrngStatefulImpl`."
  (:refer-clojure :exclude [next]))

(defprotocol PRNGStateless
  (jump [_]
   "Jump to a completly different place.")
  (uuid-seed [_]
   "Returns the uuid-seed of the random number generator.")
  (next [_]
   "Returns a new value of prng so next `peek-rnd` will return a new value.")
  (peek-int [_ a b]
   "Returns an integer between `a` and `b`.")
  (peek-double [_ a b]
   "Returns a double between `a` and `b`"))
