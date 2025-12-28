(ns auto-opti.proba-dist.registry
  "`(registry)` returns all distributions available in `auto-opti`.

  They are all implementing the `auto-opti.proba.distribution/Distribution` protocol.
  Note that you can enrich them with your own distribution implementations if needed."
  {:no-doc true}
  (:require
   [auto-opti                                 :as-alias opti]
   [auto-opti.proba-dist.impl.category        :as opt-category]
   [auto-opti.proba-dist.impl.exponential     :as opt-expo]
   [auto-opti.proba-dist.impl.kixi-stats      :as opt-kixi-stats]
   [auto-opti.proba-dist.impl.normal          :as opt-normal]
   [auto-opti.proba-dist.impl.uniform         :as opt-uniform]
   [auto-opti.proba-dist.impl.uniform-integer :as opt-uniform-int]))

(defn schema [] [:map-of :keyword fn?])

(def registry
  "Distributions registry."
  {:bernoulli (fn [_prng {::opti/keys [p]}]
                (opt-kixi-stats/make-bernoulli p))
   :beta (fn [_prng {::opti/keys [alpha beta]}]
           (opt-kixi-stats/make-beta alpha beta))
   :beta-binomial (fn [_prng {::opti/keys [n alpha beta]}]
                    (opt-kixi-stats/make-beta-binomial n alpha beta))
   :binomial (fn [_prng {::opti/keys [n p]}]
               (opt-kixi-stats/make-binomial n p))
   :categorical (fn [prng {::opti/keys [category-probabilities]}]
                  (opt-category/make prng category-probabilities))
   :cauchy (fn [_prng {::opti/keys [location scale]}]
             (opt-kixi-stats/make-cauchy location scale))
   :chi-squared (fn [_prng {::opti/keys [k]}]
                  (opt-kixi-stats/make-chi-squared k))
   :dirichlet (fn [_prng {::opti/keys [alphas]}]
                (opt-kixi-stats/make-dirichlet alphas))
   :dirichlet-multinomial (fn [_prng {::opti/keys [n alphas]}]
                            (opt-kixi-stats/make-dirichlet-multinomial n alphas))
   :exponential (fn [prng {::opti/keys [rate]}]
                  (opt-expo/make prng rate))
   :f (fn [_prng {::opti/keys [d1 d2]}]
        (opt-kixi-stats/make-f d1 d2))
   :gamma (fn [_prng {::opti/keys [shape scale]}]
            (opt-kixi-stats/make-gamma-scale shape scale))
   :log-normal (fn [_prng {::opti/keys [location scale]}]
                 (opt-kixi-stats/make-log-normal location scale))
   :multinomial (fn [_prng {::opti/keys [n probs]}]
                  (opt-kixi-stats/make-multinomial n probs))
   :normal (fn [prng {::opti/keys [location scale]}]
             (opt-normal/make prng location scale))
   :normal-integer (fn [prng {::opti/keys [location scale]}]
                     (opt-normal/make-integer prng location scale))
   :pareto (fn [_prng {::opti/keys [scale shape]}]
             (opt-kixi-stats/make-pareto scale shape))
   :poisson (fn [_prng {::opti/keys [lambda]}]
              (opt-kixi-stats/make-poisson lambda))
   :t (fn [_prng {::opti/keys [v]}]
        (opt-kixi-stats/make-t v))
   :uniform (fn [prng {::opti/keys [a b]}]
              (opt-uniform/make prng a b))
   :uniform-int (fn [prng {::opti/keys [a b]}]
                  (opt-uniform-int/make prng a b))
   :weibull (fn [_prng {::opti/keys [shape scale]}]
              (opt-kixi-stats/make-weibull shape scale))})
