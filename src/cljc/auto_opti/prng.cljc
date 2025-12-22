(ns auto-opti.prng
  "Pseudo random number generator.

  Use this feature to build a `prng` based on a parameter map. This hides the complexity of the different random number generator, as many different implementations exists and none superseeds all others."
  (:require
   [auto-opti                        :as-alias opti]
   [auto-opti.prng.impl.built-in     :as opt-prng-built-in]
   [auto-opti.prng.impl.xoroshiro128 :as opt-prng-xoro]
   [auto-opti.prng.stateful          :as opt-prng-stateful]))

(def prng-registry
  "Registry of prngs, with a function turning a seed into an implementation of `opt-prng-stateful/PRNG`."
  {:xoroshiro128
   (fn [{::opti/keys [seed]}]
     (if (string? seed) (opt-prng-xoro/make (parse-uuid seed)) (opt-prng-xoro/make seed)))
   :built-in (fn [_] (opt-prng-built-in/make))})
:64-bit
;;TODO Add metadata to describe the prng: accept-seed, :64-bit, :crosspf?
(defn prng
  "Creates a `prng` based on a parameter map."
  [{::opti/keys [prng-name seed registry]
    :as params}]
  (let [prng-name (or prng-name :xoroshiro128)
        registry (or registry prng-registry)]
    (when-let [prng-builder (get registry prng-name)]
      (prng-builder
       (assoc params ::opti/seed (or seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"))))))

(defn duplicate
  "Duplicates this prng to a new one, starting at the seed value."
  [this]
  (opt-prng-stateful/duplicate this))

(defn jump "Jump to a completly different place." [this] (opt-prng-stateful/jump this))

(defn uuid-seed
  "Returns the seed of the random number generator."
  [this]
  (opt-prng-stateful/uuid-seed this))

(defn reset
  "Returns a prng that starts again at the seed value."
  [this]
  (opt-prng-stateful/reset this))

(defn as-int
  "Returns an integer generated with `prng` between `[min-int; max-int[`."
  [prng min-int max-int]
  (when (and min-int max-int) (opt-prng-stateful/rnd-int prng min-int max-int)))

(defn as-int-pair
  "Returns a pair of random integer between `[min-int; max-int[`."
  [prng min-int max-int]
  (when (and min-int max-int)
    (let [rnd1 (as-int prng min-int max-int) rnd2 (as-int prng min-int max-int)] [rnd1 rnd2])))

(defn as-ints
  "Draw `n` random integers with `prng`, between `[min-int; max-int[`."
  [prng n min-int max-int]
  (when (and min-int max-int n) (opt-prng-stateful/as-ints prng n min-int max-int)))

(defn as-double
  "Returns a double generated with `prng` between `[min-double; max-double[`."
  [prng min-double max-double]
  (when (and min-double max-double) (opt-prng-stateful/rnd-double prng min-double max-double)))

(defn as-double-pair
  "Returns a pair of random doubles between `[min-double; max-double[`."
  [prng min-double max-double]
  (when (and min-double max-double)
    (let [rnd1 (as-int prng min-double max-double)
          rnd2 (as-int prng min-double max-double)]
      [rnd1 rnd2])))

(defn as-doubles
  "Draw `n` random doubles with `prng`, between `[min-double; max-double[`."
  [prng n min-double max-double]
  (when (and min-double max-double n)
    (repeatedly n #(opt-prng-stateful/rnd-double prng min-double max-double))))
