(ns auto-opti.distribution.impl.kixi-stats-test
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.distribution.impl.kixi-stats       :as sut]
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])))

(deftest Kixi-test
  (testing "Is bernoulli working?"
    (is (boolean? (-> (sut/make-bernoulli 0.5)
                      opt-distribution-prot/draw))))
  (testing "Is beta working?"
    (is (float? (-> (sut/make-beta 0.5 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is beta-binomial working?"
    (is (integer? (-> (sut/make-beta-binomial 2 0.5 0.7)
                      opt-distribution-prot/draw))))
  (testing "Is binomial working?"
    (is (integer? (-> (sut/make-binomial 5 0.7)
                      opt-distribution-prot/draw))))
  (testing "Is categorial working?"
    (is (keyword? (-> (sut/make-categorical {:a 0.1
                                             :b 0.3
                                             :c 0.6})
                      opt-distribution-prot/draw))))
  (testing "Is cauchy working?"
    (is (float? (-> (sut/make-cauchy 0.4 0.6)
                    opt-distribution-prot/draw))))
  (testing "Is chi-squared working?"
    (is (float? (-> (sut/make-chi-squared 3)
                    opt-distribution-prot/draw))))
  (testing "Is dirichlet working?"
    (is (every? float?
                (-> (sut/make-dirichlet [3 4.0])
                    opt-distribution-prot/draw))))
  (testing "Is dirichlet multinomial working?"
    (is (every? integer?
                (-> (sut/make-dirichlet-multinomial 3 [3 4.0])
                    opt-distribution-prot/draw))))
  (testing "Is exponential working?"
    (is (float? (-> (sut/make-exponential 0.4)
                    opt-distribution-prot/draw))))
  (testing "Is f working?"
    (is (float? (-> (sut/make-f 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is gamma rate working?"
    (is (float? (-> (sut/make-gamma-rate 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is gamma scale working?"
    (is (float? (-> (sut/make-gamma-scale 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is log normal working?"
    (is (float? (-> (sut/make-log-normal 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is multinomial working?"
    (is (every? integer?
                (-> (sut/make-multinomial 12 [0.4 0.7])
                    opt-distribution-prot/draw))))
  (testing "Is normal working?"
    (is (float? (-> (sut/make-normal 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is pareto working?"
    (is (float? (-> (sut/make-pareto 0.4 0.7)
                    opt-distribution-prot/draw))))
  (testing "Is poisson working?"
    (is (integer? (-> (sut/make-poisson 0.4)
                      opt-distribution-prot/draw))))
  (testing "Is t working?"
    (is (float? (-> (sut/make-t 0.4)
                    opt-distribution-prot/draw))))
  (testing "Is uniform working?"
    (is (float? (-> (sut/make-uniform 0.4 1.4)
                    opt-distribution-prot/draw))))
  (testing "Is weibull working?"
    (is (float? (-> (sut/make-weibull 0.4 1.4)
                    opt-distribution-prot/draw)))))
