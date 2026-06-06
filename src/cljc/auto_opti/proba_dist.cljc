(ns auto-opti.proba-dist
  "Probabilitisc distributions."
  (:refer-clojure :exclude [resolve])
  (:require
   [auto-opti                                  :as-alias opti]
   [auto-opti.prng                             :as opt-prng]
   [auto-opti.proba-dist.distribution-protocol :as opt-distribution-prot]
   [auto-opti.proba-dist.impl.factory          :as opt-distribution-factory]
   [auto-opti.proba-dist.registry              :as opt-random-registry]))

(def distribution-registry "Returns the base registry." opt-random-registry/registry)

(defn distribution
  "Returns a `distribution` created with the following parameters:

  If provided, the following parameters are used:
  * `registry` is where `dstb-name` will be searched for,
  * `dstb-name` is the name of the distribution, defaulted to `uniform`,
  * `prng` if not provided, a `prng` is created with name `prng-name` and `seed`,
  * `params` the parameters of that distribution, check `dstb-name` definition to know what parameters are necessary.,
  * `seed` if no `prng` is provided, it is built with `seed`,
  * `prng-name` the pseudo random number generator that is used to generate the distribution."
  [params]
  (cond
    (map? params) (let [{::opti/keys [registry dstb-name prng seed prng-name]} params]
                    (opt-distribution-factory/build (or registry distribution-registry)
                                                    (or dstb-name :uniform)
                                                    (or prng
                                                        (opt-prng/prng #::opti{:prng-name prng-name
                                                                               :seed seed}))
                                                    params))
    (number? params) params
    :else nil))

(defn draw
  "Returns a random value following the `distribution`."
  [distribution]
  (opt-distribution-prot/draw distribution))

(defn draws [distribution n] (repeatedly n #(opt-distribution-prot/draw distribution)))

(def minimum opt-distribution-prot/minimum)

(def maximum opt-distribution-prot/maximum)

(def quantile opt-distribution-prot/quantile)

(defn median
  "Returns the median of the distribution"
  [distribution]
  (opt-distribution-prot/median distribution))

(defn cumulative
  "Returns the cumulative probability before `p`"
  [this p]
  (opt-distribution-prot/cumulative this p))

(defn resolve
  "Turns a `distribution` into its actual value"
  [dstb]
  (if (number? dstb) dstb (draw dstb)))

