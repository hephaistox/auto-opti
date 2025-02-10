(ns auto-opti.distribution
  "Probabilitisc distributions."
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.distribution.impl.factory          :as opt-distribution-factory]
   [auto-opti.distribution.registry              :as opt-random-registry]
   [auto-opti.prng                               :as opt-prng]))

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
  [{:keys [registry dstb-name prng params seed prng-name]
    :or {prng-name :xoroshiro128
         dstb-name :uniform
         registry distribution-registry}}]
  (opt-distribution-factory/build registry
                                  dstb-name
                                  (or prng
                                      (opt-prng/prng (cond-> {:prng-name prng-name}
                                                       seed (assoc :seed seed))))
                                  params))

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
