(ns auto-opti.distribution.registry
  "`(registry)` returns all distributions available in `auto-opti`.

  They are all implementing the `auto-opti.proba.distribution/Distribution` protocol.
  Note that you can enrich them with your own distribution implementations if needed."
  (:require
   [auto-opti.distribution                      :as-alias opt-dist]
   [auto-opti.distribution.impl.category        :as opt-category]
   [auto-opti.distribution.impl.exponential     :as opt-expo]
   [auto-opti.distribution.impl.kixi-stats      :as opt-kixi-stats]
   [auto-opti.distribution.impl.uniform         :as opt-uniform]
   [auto-opti.distribution.impl.uniform-integer :as opt-uniform-int]
   [auto-opti.maths                             :as opt-maths]))

(defn schema [] [:map-of :keyword fn?])

(def registry
  "Distributions registry."
  {:bernoulli (fn [_prng {::opt-dist/keys [p]}]
                (opt-kixi-stats/make-bernoulli p))
   :beta (fn [_prng {::opt-dist/keys [alpha beta]}]
           (opt-kixi-stats/make-beta alpha beta))
   :beta-binomial (fn [_prng {::opt-dist/keys [n alpha beta]}]
                    (opt-kixi-stats/make-beta-binomial n alpha beta))
   :binomial (fn [_prng {::opt-dist/keys [n p]}]
               (opt-kixi-stats/make-binomial n p))
   :categorical (fn [prng {::opt-dist/keys [category-probabilities]}]
                  (opt-category/make prng category-probabilities))
   :cauchy (fn [_prng {::opt-dist/keys [location scale]}]
             (opt-kixi-stats/make-cauchy location scale))
   :chi-squared (fn [_prng {::opt-dist/keys [k]}]
                  (opt-kixi-stats/make-chi-squared k))
   :dirichlet (fn [_prng {::opt-dist/keys [alphas]}]
                (opt-kixi-stats/make-dirichlet alphas))
   :dirichlet-multinomial (fn [_prng {::opt-dist/keys [n alphas]}]
                            (opt-kixi-stats/make-dirichlet-multinomial n alphas))
   :exponential (fn [prng {::opt-dist/keys [rate]}]
                  (opt-expo/make prng rate))
   :f (fn [_prng {::opt-dist/keys [d1 d2]}]
        (opt-kixi-stats/make-f d1 d2))
   :gamma (fn [_prng {::opt-dist/keys [shape scale]}]
            (opt-kixi-stats/make-gamma-scale shape scale))
   :log-normal (fn [_prng {::opt-dist/keys [location scale]}]
                 (opt-kixi-stats/make-log-normal location scale))
   :multinomial (fn [_prng {::opt-dist/keys [n probs]}]
                  (opt-kixi-stats/make-multinomial n probs))
   :normal (fn [_prng {::opt-dist/keys [location scale]}]
             (opt-kixi-stats/make-normal location scale))
   :pareto (fn [_prng {::opt-dist/keys [scale shape]}]
             (opt-kixi-stats/make-pareto scale shape))
   :poisson (fn [_prng {::opt-dist/keys [lambda]}]
              (opt-kixi-stats/make-poisson lambda))
   :t (fn [_prng {::opt-dist/keys [v]}]
        (opt-kixi-stats/make-t v))
   :uniform (fn [prng {::opt-dist/keys [a b]}]
              (opt-uniform/make prng (or a 0) (or b opt-maths/interop-max-integer)))
   :uniform-int (fn [prng {::opt-dist/keys [a b]}]
                  (opt-uniform-int/make prng (or a 0) (or b opt-maths/interop-max-integer)))
   :weibull (fn [_prng {::opt-dist/keys [shape scale]}]
              (opt-kixi-stats/make-weibull shape scale))})
