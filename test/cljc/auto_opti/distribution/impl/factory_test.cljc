(ns auto-opti.distribution.impl.factory-test
  (:require
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
                      (sut/build :bernoulli (prng) {:p 0.3})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :beta
                               (prng)
                               {:beta 0.5
                                :alpha 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :beta-binomial
                                 (prng)
                                 {:n 2
                                  :alpha 0.5
                                  :beta 0.7})
                      opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :binomial
                                 (prng)
                                 {:n 5
                                  :p 0.7})
                      opt-distribution-prot/draw)))
    (is (keyword? (-> opt-random-registry/registry
                      (sut/build :categorical
                                 (prng)
                                 {:category-probabilities {:a 0.1
                                                           :b 0.3
                                                           :c 0.6}})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :cauchy
                               (prng)
                               {:location 0.4
                                :scale 0.6})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :chi-squared (prng) {:k 3})
                    opt-distribution-prot/draw)))
    (is (every? float?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet (prng) {:alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet-multinomial
                               (prng)
                               {:n 3
                                :alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :exponential (prng) {:rate 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :f
                               (prng)
                               {:d1 0.4
                                :d2 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :gamma
                               (prng)
                               {:shape 0.4
                                :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :log-normal
                               (prng)
                               {:location 0.4
                                :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :multinomial
                               (prng)
                               {:n 12
                                :probs [0.4 0.7]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :normal
                               (prng)
                               {:location 0.4
                                :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :pareto
                               (prng)
                               {:scale 0.4
                                :shape 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :poisson (prng) {:lambda 0.4})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :t (prng) {:v 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :uniform
                               (prng)
                               {:a 0.4
                                :b 1.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :weibull
                               (prng)
                               {:shape 0.4
                                :scale 1.4})
                    opt-distribution-prot/draw)))))
