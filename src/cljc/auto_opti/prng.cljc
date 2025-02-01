(ns auto-opti.prng
  "Pseudo random number generator.
  This protocol hides the complexity of the different random number generator. As many different implementations exists and none superseeds all others."
  (:require
   [auto-opti.prng.impl.built-in     :as opt-prng-built-in]
   [auto-opti.prng.impl.xoroshiro128 :as opt-prng-xoro]
   [auto-opti.prng.stateful          :as opt-prng-stateful]))

(defn xoroshiro128
  "Creates a xoroshiro128 prng instance, the optional `params` parameter can be supplied to force."
  ([] (opt-prng-xoro/make))
  ([seed] (opt-prng-xoro/make seed)))

(defn built-in
  "Creates an instance of the built-in prng of your platform (java or javascript)."
  []
  (opt-prng-built-in/make))

(def prng-registry
  {:xoroshiro128 (fn [{:keys [seed]}] (xoroshiro128 seed))
   :built-in (fn [_] (built-in))})

(defn prng
  "Creates a `prng` with or without seed."
  [{:keys [prng-name seed registry]
    :or {seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"
         registry prng-registry}
    :as params}]
  (try (when-let [prng-builder (get registry prng-name)] (prng-builder (assoc params :seed seed)))
       (catch #?(:clj Exception
                 :cljs :default)
         _
         nil)))

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

(defn as-doubles
  "Draw `n` random doubles with `prng`, between `[min-int; max-int[`."
  [prng n min-int max-int]
  (when (and min-int max-int n)
    (repeatedly n #(opt-prng-stateful/rnd-double prng min-int max-int))))
