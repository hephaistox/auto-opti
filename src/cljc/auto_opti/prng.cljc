(ns auto-opti.prng
  "Pseudo random number generator.

  Use this feature to build a `prng` based on a parameter map. This hides the complexity of the different random number generator, as many different implementations exists and none superseeds all others.

  SECURITY - these generators (xoroshiro/xoshiro and the host built-in) are NOT cryptographically secure. Never use them for keys, tokens, password salts, nonces or any security-sensitive randomness; use a CSPRNG instead. Also note the default seed is a fixed uuid, so a `prng` built without an explicit `::opti/seed` always produces the SAME stream - intended for reproducible simulation, but a footgun anywhere uniqueness is assumed."
  (:require
   [auto-opti                        :as-alias opti]
   [auto-opti.prng.impl.built-in     :as opt-prng-built-in]
   [auto-opti.prng.impl.xoroshiro128 :as opt-prng-xoro]
   #?(:clj [auto-opti.prng.impl.xoroshiro128-jvm :as opt-prng-xoro-fast]
      :cljs [auto-opti.prng.impl.xoroshiro128-js :as opt-prng-xoro-fast])
   #?(:clj [auto-opti.prng.impl.xoroshiro256-jvm :as opt-prng-xoro-256])
   [auto-opti.prng.stateful          :as opt-prng-stateful]))

(def seed-schema
  "Malli schema documenting `::opti/seed`: a uuid, or a string parseable as a uuid."
  [:or
   :uuid
   [:and
    :string
    [:fn {:error/message "not a valid uuid string"}
     #(some? (parse-uuid %))]]])

(defn- ->uuid
  "Coerce a `seed` (uuid or uuid string) to a uuid, throwing a clear error on
  malformed input instead of failing later with an obscure exception."
  [seed]
  (cond
    (uuid? seed) seed
    (string? seed) (or (parse-uuid seed)
                       (throw (ex-info "Invalid ::opti/seed: string is not a valid uuid"
                                       {:seed seed
                                        :schema seed-schema})))
    :else (throw (ex-info "Invalid ::opti/seed: expected a uuid or uuid string"
                          {:seed seed
                           :schema seed-schema}))))

(def prng-registry
  "Registry of prngs, with a function turning a seed into an implementation of `opt-prng-stateful/PRNG`.

  * `:xoroshiro128` - portable, returns the exact same values on the JVM and in
    JavaScript, at an efficiency cost.
  * `:xoroshiro128-jvm` / `:xoroshiro256-jvm` (JVM only), `:xoroshiro128-js` (JS
    only) - the fastest implementations on each platform. Only the entries
    matching the host platform are registered, so a platform-specific key cannot
    be used across platforms.

  TODO Add metadata to describe each prng: accept-seed, :64-bit, :cross-pf?"
  (merge {:xoroshiro128 (fn [{::opti/keys [seed]}] (opt-prng-xoro/make (->uuid seed)))
          :built-in (fn [_] (opt-prng-built-in/make))}
         #?(:clj
              {:xoroshiro128-jvm (fn [{::opti/keys [seed]}] (opt-prng-xoro-fast/make (->uuid seed)))
               :xoroshiro256-jvm (fn [{::opti/keys [seed]}] (opt-prng-xoro-256/make (->uuid seed)))}
            :cljs {:xoroshiro128-js (fn [{::opti/keys [seed]}]
                                      (opt-prng-xoro-fast/make (->uuid seed)))})))

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
