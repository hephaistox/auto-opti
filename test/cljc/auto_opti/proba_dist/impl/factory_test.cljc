(ns auto-opti.proba-dist.impl.factory-test
  (:require
   [auto-opti                                  :as-alias opti]
   [auto-opti.prng.impl.xoroshiro128           :as opt-prng-xoro]
   [auto-opti.proba-dist.distribution-protocol :as opt-distribution-prot]
   [auto-opti.proba-dist.impl.factory          :as sut]
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])
   [auto-opti.proba-dist.registry              :as opt-random-registry]))

(def seed #uuid "9fa7b74d-2414-4815-9cfd-a7b9af715b6a")

(defn prng [] (opt-prng-xoro/make seed))

(deftest build-test
  (testing "Are builder working for all variants"
    (is (boolean? (-> opt-random-registry/registry
                      (sut/build :bernoulli (prng) #::opti{:p 0.3})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :beta
                               (prng)
                               #::opti{:beta 0.5
                                       :alpha 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :beta-binomial
                                 (prng)
                                 #::opti{:n 2
                                         :alpha 0.5
                                         :beta 0.7})
                      opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :binomial
                                 (prng)
                                 #::opti{:n 5
                                         :p 0.7})
                      opt-distribution-prot/draw)))
    (is (keyword? (-> opt-random-registry/registry
                      (sut/build :categorical
                                 (prng)
                                 #::opti{:category-probabilities {:a 0.1
                                                                  :b 0.3
                                                                  :c 0.6}})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :cauchy
                               (prng)
                               #::opti{:location 0.4
                                       :scale 0.6})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :chi-squared (prng) #::opti{:k 3})
                    opt-distribution-prot/draw)))
    (is (every? float?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet (prng) #::opti{:alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :dirichlet-multinomial
                               (prng)
                               #::opti{:n 3
                                       :alphas [3 4.0]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :exponential (prng) #::opti{:rate 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :f
                               (prng)
                               #::opti{:d1 0.4
                                       :d2 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :gamma
                               (prng)
                               #::opti{:shape 0.4
                                       :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :log-normal
                               (prng)
                               #::opti{:location 0.4
                                       :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (every? integer?
                (-> opt-random-registry/registry
                    (sut/build :multinomial
                               (prng)
                               #::opti{:n 12
                                       :probs [0.4 0.7]})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :normal
                               (prng)
                               #::opti{:location 0.4
                                       :scale 0.7})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :pareto
                               (prng)
                               #::opti{:scale 0.4
                                       :shape 0.7})
                    opt-distribution-prot/draw)))
    (is (integer? (-> opt-random-registry/registry
                      (sut/build :poisson (prng) #::opti{:lambda 0.4})
                      opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :t (prng) #::opti{:v 0.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :uniform
                               (prng)
                               #::opti{:a 0.4
                                       :b 1.4})
                    opt-distribution-prot/draw)))
    (is (float? (-> opt-random-registry/registry
                    (sut/build :weibull
                               (prng)
                               #::opti{:shape 0.4
                                       :scale 1.4})
                    opt-distribution-prot/draw)))))
