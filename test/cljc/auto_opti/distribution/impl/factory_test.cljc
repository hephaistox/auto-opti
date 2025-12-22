(ns auto-opti.distribution.impl.factory-test
  (:require
   [auto-opti.distribution                       :as-alias opt-dstb]
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.distribution.impl.factory          :as sut]
   [auto-opti.distribution.registry              :as opt-random-registry]
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])
   [auto-opti.prng.impl.xoroshiro128             :as opt-prng-xoro]))

(defn prng [] (opt-prng-xoro/make))

(deftest build-test
  (testing "Are builder working for all variants"
    (is (boolean? (-> opt-random-registry/registry
                      (sut/build :bernoulli (prng) #::opt-dstb{:p 0.3})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :beta
                               (prng)
                               #::opt-dstb{:beta 0.5
                                           :alpha 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :beta-binomial
                                 (prng)
                                 #::opt-dstb{:n 2
                                             :alpha 0.5
                                             :beta 0.7})
                      opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :binomial
                                 (prng)
                                 #::opt-dstb{:n 5
                                             :p 0.7})
                      opt-distribution-prot/draw)))
    (is (keyword? (-> opt-random-registry/registry
                      (sut/build :categorical
                                 (prng)
                                 #::opt-dstb{:category-probabilities {:a 0.1
                                                                      :b 0.3
                                                                      :c 0.6}})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :cauchy
                               (prng)
                               #::opt-dstb{:location 0.4
                                           :scale 0.6})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :chi-squared (prng) #::opt-dstb{:k 3})
                    opt-distribution-prot/draw)))
    (is (every? float?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet (prng) #::opt-dstb{:alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet-multinomial
                               (prng)
                               #::opt-dstb{:n 3
                                           :alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :exponential (prng) #::opt-dstb{:rate 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :f
                               (prng)
                               #::opt-dstb{:d1 0.4
                                           :d2 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :gamma
                               (prng)
                               #::opt-dstb{:shape 0.4
                                           :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :log-normal
                               (prng)
                               #::opt-dstb{:location 0.4
                                           :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :multinomial
                               (prng)
                               #::opt-dstb{:n 12
                                           :probs [0.4 0.7]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :normal
                               (prng)
                               #::opt-dstb{:location 0.4
                                           :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :pareto
                               (prng)
                               #::opt-dstb{:scale 0.4
                                           :shape 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :poisson (prng) #::opt-dstb{:lambda 0.4})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :t (prng) #::opt-dstb{:v 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :uniform
                               (prng)
                               #::opt-dstb{:a 0.4
                                           :b 1.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :weibull
                               (prng)
                               #::opt-dstb{:shape 0.4
                                           :scale 1.4})
                    opt-distribution-prot/draw)))))
