(ns auto-opti.distribution.registry
  "`(registry)` returns all distributions available in `auto-opti`.

  They are all implementing the `auto-opti.proba.distribution/Distribution` protocol.
  Note that you can enrich them with your own distribution implementations if needed."
  (:require
   [auto-opti.distribution.impl.exponential     :as opt-expo]
   [auto-opti.distribution.impl.kixi-stats      :as opt-kixi-stats]
   [auto-opti.distribution.impl.uniform         :as opt-uniform]
   [auto-opti.distribution.impl.uniform-integer :as opt-uniform-int]
   [auto-opti.maths                             :as opt-maths]))

(defn schema [] [:map-of :keyword fn?])

(def registry
  "Distributions registry."
  {:bernoulli (fn [_prng {:keys [p]}] (opt-kixi-stats/make-bernoulli p))
   :beta (fn [_prng {:keys [alpha beta]}] (opt-kixi-stats/make-beta alpha beta))
   :beta-binomial (fn [_prng {:keys [n alpha beta]}]
                    (opt-kixi-stats/make-beta-binomial n alpha beta))
   :binomial (fn [_prng {:keys [n p]}] (opt-kixi-stats/make-binomial n p))
   :categorical (fn [_prng {:keys [category-probabilities]}]
                  (opt-kixi-stats/make-categorical category-probabilities))
   :cauchy (fn [_prng {:keys [location scale]}] (opt-kixi-stats/make-cauchy location scale))
   :chi-squared (fn [_prng {:keys [k]}] (opt-kixi-stats/make-chi-squared k))
   :dirichlet (fn [_prng {:keys [alphas]}] (opt-kixi-stats/make-dirichlet alphas))
   :dirichlet-multinomial (fn [_prng {:keys [n alphas]}]
                            (opt-kixi-stats/make-dirichlet-multinomial n alphas))
   :exponential (fn [prng
                     {:keys [rate]
                      :as _law-parameters}]
                  (opt-expo/make prng rate))
   :f (fn [_prng {:keys [d1 d2]}] (opt-kixi-stats/make-f d1 d2))
   :gamma (fn [_prng {:keys [shape scale]}] (opt-kixi-stats/make-gamma-scale shape scale))
   :log-normal (fn [_prng {:keys [location scale]}] (opt-kixi-stats/make-log-normal location scale))
   :multinomial (fn [_prng {:keys [n probs]}] (opt-kixi-stats/make-multinomial n probs))
   :normal (fn [_prng {:keys [location scale]}] (opt-kixi-stats/make-normal location scale))
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
