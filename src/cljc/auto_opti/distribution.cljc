(ns auto-opti.distribution
  "Distribution api"
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.distribution.impl.factory          :as opt-distribution-factory]
   [auto-opti.distribution.registry              :as opt-random-registry]
   [auto-opti.prng                               :as opt-prng]))

(def distribution-registry "Returns the base registry." opt-random-registry/registry)

(defn distribution
  "Returns a `distribution` created with the following parameters:

  If provided, the following parameters are higher priority:

  * `registry` is where `distribution-name` will be searched for,
  * `distribution-name` is the name of the distribution as found in the `registry`,
  * `prng` the pseudo random number generator that is used to generate the distribution,
  * `params` the parameters of that distribution

  If not provided:

  * `seed` if not provided,
  * `prng` if not provided, a `prng` is created with name `prng-name` and `seed`,
  * `distribution-name` is defaulted to `:uniform`,
  * `registry` is defaulted with the `distribution-registry`."
  ([] (distribution {}))
  ([{registry :registry
     distribution-name :dstb-name
     prng-param :prng
     params :params
     seed :seed
     prng-name :prng-name
     :or {prng-name :xoroshiro128
          distribution-name :uniform
          registry distribution-registry}}]
   (opt-distribution-factory/build registry
                                   distribution-name
                                   (or prng-param
                                       (opt-prng/prng (cond-> {:prng-name prng-name}
                                                        seed (assoc :seed seed))))
                                   params)))

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
