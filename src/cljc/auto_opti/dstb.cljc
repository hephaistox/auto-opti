(ns auto-opti.dstb
  "Distribution api"
  (:refer-clojure :exclude [resolve])
  (:require
   [auto-opti.dstb.dstb-protocol            :as opt-dstb-prot]
   [auto-opti.dstb.impl.category            :as opt-category]
   [auto-opti.dstb.impl.exponential         :as opt-expo]
   [auto-opti.dstb.impl.exponential-integer :as opt-expo-int]
   [auto-opti.dstb.impl.kixi-stats          :as opt-kixi-stats]
   [auto-opti.dstb.impl.normal              :as opt-normal]
   [auto-opti.dstb.impl.uniform             :as opt-uniform]
   [auto-opti.dstb.impl.uniform-integer     :as opt-uniform-int]
   [auto-opti.maths                         :as opt-maths]
   [auto-opti.prng                          :as opt-prng]))

(defn factory
  "Creates the distribution `dstb-name` leveraging the `prng`.
  The distribution will be found in the `registry`.
  As most of distribution needs parameters, they will be found in the `law-parameters`."
  [registry dstb-name prng law-parameters]
  (when-let [law (get registry dstb-name)] (law prng law-parameters)))

(def registry
  "Registry with predefined `distribution`."
  {:bernoulli (fn [_prng {:keys [p]}] (opt-kixi-stats/make-bernoulli p))
   :beta (fn [_prng {:keys [alpha beta]}] (opt-kixi-stats/make-beta alpha beta))
   :beta-binomial (fn [_prng {:keys [n alpha beta]}]
                    (opt-kixi-stats/make-beta-binomial n alpha beta))
   :binomial (fn [_prng {:keys [n p]}] (opt-kixi-stats/make-binomial n p))
   :categorical (fn [prng {:keys [category-probabilities]}]
                  (opt-category/make prng category-probabilities))
   :cauchy (fn [_prng {:keys [location scale]}] (opt-kixi-stats/make-cauchy location scale))
   :chi-squared (fn [_prng {:keys [k]}] (opt-kixi-stats/make-chi-squared k))
   :dirichlet (fn [_prng {:keys [alphas]}] (opt-kixi-stats/make-dirichlet alphas))
   :dirichlet-multinomial (fn [_prng {:keys [n alphas]}]
                            (opt-kixi-stats/make-dirichlet-multinomial n alphas))
   :exponential (fn [prng
                     {:keys [rate]
                      :as _law-parameters}]
                  (opt-expo/make prng rate))
   :exponential-integer (fn [prng
                             {:keys [rate]
                              :as _law-parameters}]
                          (opt-expo-int/make prng rate))
   :f (fn [_prng {:keys [d1 d2]}] (opt-kixi-stats/make-f d1 d2))
   :gamma (fn [_prng {:keys [shape scale]}] (opt-kixi-stats/make-gamma-scale shape scale))
   :log-normal (fn [_prng {:keys [location scale]}] (opt-kixi-stats/make-log-normal location scale))
   :multinomial (fn [_prng {:keys [n probs]}] (opt-kixi-stats/make-multinomial n probs))
   :normal (fn [prng {:keys [location scale]}] (opt-normal/make prng location scale))
   :normal-integer (fn [prng {:keys [location scale]}]
                     (opt-normal/make-integer prng location scale))
   :pareto (fn [_prng {:keys [scale shape]}] (opt-kixi-stats/make-pareto scale shape))
   :poisson (fn [_prng {:keys [lambda]}] (opt-kixi-stats/make-poisson lambda))
   :t (fn [_prng {:keys [v]}] (opt-kixi-stats/make-t v))
   :uniform (fn [prng
                 {:keys [a b]
                  :or {a 0
                       b opt-maths/interop-max-integer}}]
              (opt-uniform/make prng a b))
   :uniform-int (fn [prng
                     {:keys [a b]
                      :or {a 0
                           b opt-maths/interop-max-integer}}]
                  (opt-uniform-int/make prng a b))
   :weibull (fn [_prng {:keys [shape scale]}] (opt-kixi-stats/make-weibull shape scale))})

(defn dstb
  "`dstb` created with the following parameters:

  If provided, the following parameters are higher priority:

  * `registry` is where `dstb-name` will be searched for,
  * `dstb-name` is the name of the distribution as found in the `registry`,
  * `prng` the pseudo random number generator that is used to generate the distribution,
  * `params` the parameters of that distribution

  If not provided:

  * `seed` if not provided,
  * `prng` if not provided, a `prng` is created with name `prng-name` and `seed`,
  * `dstb-name` is defaulted to `:uniform`,
  * `registry` is defaulted with the `dstb-registry`."
  ([] (dstb {}))
  ([law-params]
   (if (number? law-params)
     law-params
     (let [{registry :registry
            dstb-name :dstb-name
            prng-from-param :prng
            seed :seed
            prng-name :prng-name
            :or {prng-name :xoroshiro128
                 dstb-name :uniform
                 registry registry}}
           law-params]
       (factory registry
                dstb-name
                (or prng-from-param
                    (opt-prng/prng (cond-> {:prng-name prng-name}
                                     seed (assoc :seed seed))))
                law-params))))
  ([prng params] (dstb (if (number? params) params (assoc params :prng prng)))))

(defn draw "Returns a random value following the `dstb`." [dstb] (opt-dstb-prot/draw dstb))

(defn draws [dstb n] (repeatedly n #(opt-dstb-prot/draw dstb)))

(def minimum opt-dstb-prot/minimum)

(def maximum opt-dstb-prot/maximum)

(def quantile opt-dstb-prot/quantile)

(defn median "Returns the median of the dstb" [dstb] (opt-dstb-prot/median dstb))

(defn cumulative
  "Returns the cumulative probability before `p`"
  [dstb p]
  (opt-dstb-prot/cumulative dstb p))

(defn resolve
  "Turns a `distribution` into its actual value"
  [dstb]
  (if (number? dstb) dstb (draw dstb)))
