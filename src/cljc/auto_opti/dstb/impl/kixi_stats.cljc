(ns auto-opti.dstb.impl.kixi-stats
  "Proxy for kixi-stats distributions.

  See the [github repo](https://github.com/MastodonC/kixi.stats)."
  (:require
   [auto-opti.dstb.dstb-protocol :as opt-dstb-prot]
   [kixi.stats.distribution      :as kixi-dstb]))

(defrecord Kixi [kixi-object]
  opt-dstb-prot/Distribution
    (draw [_] (first (kixi-dstb/sample 1 kixi-object)))
    (median [_] (kixi-dstb/median kixi-object))
    (cumulative [_ p] (kixi-dstb/cdf kixi-object p))
    (minimum [_] (kixi-dstb/minimum kixi-object))
    (maximum [_] (kixi-dstb/maximum kixi-object))
    (quantile [_ x] (kixi-dstb/quantile kixi-object x)))

(defn make-bernoulli
  "Returns a Bernoulli distribution. `{:p ∈ [0 1]}`."
  [p]
  (when (and (number? p) (<= 0.0 p 1.0)) (->Kixi (kixi-dstb/bernoulli {:p p}))))

(defn make-beta
  "Returns a beta distribution. `{:alpha ∈ ℝ > 0, :beta ∈ ℝ > 0}`."
  [alpha beta]
  (when (and (pos? alpha) (pos? beta))
    (->Kixi (kixi.stats.distribution/beta {:alpha alpha
                                           :beta beta}))))

(defn make-beta-binomial
  "Returns a beta distribution. `{:n ∈ ℕ > 0, :alpha ∈ ℝ > 0, :beta ∈ ℝ > 0}`."
  [n alpha beta]
  (when (and (pos-int? n) (number? alpha) (number? beta) (pos? alpha) (pos? beta))
    (->Kixi (kixi.stats.distribution/beta-binomial {:n n
                                                    :alpha alpha
                                                    :beta beta}))))

(defn make-binomial
  "Return a binomial distribution. `{:n ∈ ℕ, :p ∈ [0 1]}`."
  [n p]
  (when (and (nat-int? n) (number? p) (<= 0.0 p 1.0))
    (->Kixi (kixi-dstb/binomial {:n n
                                 :p p}))))

(defn make-categorical
  "Returns a categorical distribution. `{[category] [probability], ...}` Probabilities should be >= 0 and sum to 1."
  [category-probabilities]
  (when (every? #(and (number? %) (<= 0 % 1)) (vals category-probabilities))
    (->Kixi (kixi-dstb/categorical category-probabilities))))

(defn make-cauchy
  "Returns a Cauchy distribution. `{:location ∈ ℝ, :scale ∈ ℝ > 0}`."
  [location scale]
  (when (and (number? location) (number? scale) (pos? scale))
    (->Kixi (kixi-dstb/cauchy {:location location
                               :scale scale}))))

(defn make-chi-squared
  "Returns a chi-squared distribution. `{:k ∈ ℕ > 0}`."
  [k]
  (when (pos-int? k) (->Kixi (kixi-dstb/chi-squared {:k k}))))

(defn make-dirichlet
  "Returns a Dirichlet distribution. `{:alphas [ℝ >= 0, ...]}`."
  [alphas]
  (when (every? #(and (number? %) (>= % 0)) alphas)
    (->Kixi (kixi-dstb/dirichlet {:alphas alphas}))))

(defn make-dirichlet-multinomial
  "Returns a Dirichlet-multinomial distribution. `{:n ∈ ℕ, :alphas [ℝ >= 0, ...]}`."
  [n alphas]
  (when (and (nat-int? n) (every? #(and (number? %) (>= % 0)) alphas))
    (->Kixi (kixi-dstb/dirichlet-multinomial {:alphas alphas
                                              :n n}))))

(defn make-exponential
  "Returns an exponential distribution. `{:rate ∈ ℝ > 0}`."
  [rate]
  (when (and (number? rate) (pos? rate)) (->Kixi (kixi-dstb/exponential {:rate rate}))))

(defn make-f
  "Returns an F distribution. `{:d1 ∈ ℝ > 0, :d2 ∈ ℝ > 0}`."
  [d1 d2]
  (when (and (number? d1) (number? d2) (pos? d1) (pos? d2))
    (->Kixi (kixi-dstb/f {:d1 d1
                          :d2 d2}))))

(defn make-gamma-rate
  "Returns a gamma distribution. `{:shape ∈ ℝ > 0, :rate ∈ ℝ > 0}`."
  [shape rate]
  (when (and (number? shape) (number? rate) (pos? shape) (pos? rate))
    (->Kixi (kixi-dstb/gamma {:shape shape
                              :rate rate}))))

(defn make-gamma-scale
  "Returns a gamma distribution. `{:shape ∈ ℝ > 0, :scale ∈ ℝ > 0}`."
  [shape scale]
  (when (and (number? shape) (number? scale) (pos? shape) (pos? scale))
    (->Kixi (kixi-dstb/gamma {:shape shape
                              :scale scale}))))

(defn make-log-normal
  "Returns a Log-normal distribution. The parameters are the log of the mean and sd of this distribution. `{:location ∈ ℝ, :scale ∈ ℝ > 0}`."
  [location scale]
  (when (and (number? location) (number? scale) (pos? scale))
    (->Kixi (kixi.stats.distribution/log-normal {:location location
                                                 :scale scale}))))

(defn make-multinomial
  "Returns a multinomial distribution. `{:n ∈ ℕ > 0, :probs [ℝ >= 0, ...]}` Probabilities should be >= 0 and sum to 1."
  [n probs]
  (when (and (pos-int? n) (every? #(and (number? %) (<= 0 % 1)) probs))
    (->Kixi (kixi.stats.distribution/multinomial {:n n
                                                  :probs probs}))))

(defn make-normal
  "Returns a normal distribution. `{:location ∈ ℝ, :scale ∈ ℝ > 0}`."
  [location scale]
  (when (and (number? location) (number? scale) (pos? scale))
    (->Kixi (kixi.stats.distribution/normal {:location location
                                             :scale scale}))))

(defn make-pareto
  "Returns a Pareto distribution. `{:scale ∈ ℝ > 0, :shape ∈ ℝ > 0}`."
  [scale shape]
  (when (and (number? scale) (number? shape) (pos? scale) (pos? shape))
    (->Kixi (kixi.stats.distribution/pareto {:shape shape
                                             :scale scale}))))

(defn make-poisson
  "Returns a Poisson distribution. `{:lambda ∈ ℝ > 0}`."
  [lambda]
  (when (and (number? lambda) (pos? lambda))
    (->Kixi (kixi.stats.distribution/poisson {:lambda lambda}))))

(defn make-t
  "Returns a t distribution. `{:v ∈ ℝ > 0}`."
  [v]
  (when (and (number? v) (pos? v)) (->Kixi (kixi.stats.distribution/t {:v v}))))

(defn make-uniform
  "Returns a uniform distribution. `{:a ∈ ℝ, :b ∈ ℝ, :a < :b}`."
  [a b]
  (when (and (number? a) (number? b) (<= a b))
    (->Kixi (kixi.stats.distribution/uniform {:a a
                                              :b b}))))

(defn make-weibull
  "Returns a weibull distribution. `{:shape ∈ ℝ >= 0, :scale ∈ ℝ >= 0}`."
  [scale shape]
  (when (and (number? shape) (number? scale) (>= shape 0) (>= scale 0))
    (->Kixi (kixi.stats.distribution/weibull {:shape shape
                                              :scale scale}))))
