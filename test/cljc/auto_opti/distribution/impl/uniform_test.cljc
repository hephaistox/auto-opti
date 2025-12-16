(ns auto-opti.distribution.impl.uniform-test
  (:require
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])
   [auto-opti.distribution.distribution-protocol :as opt-distribution-prot]
   [auto-opti.distribution.impl.uniform          :as sut]
   [auto-opti.maths                              :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128             :as opt-prng-xoro]))

(deftest uniform-test
  (is (every? double?
              (repeatedly 10
                          (fn []
                            (-> (opt-prng-xoro/make)
                                (sut/make 2 60)
                                opt-distribution-prot/draw))))
      "Uniform distribution returns elements in the range")
  (is (= 31.0
         (-> (opt-prng-xoro/make)
             (sut/make 2 60)
             opt-distribution-prot/median))
      "Uniform distribution returns elements in the range")
  (testing "Test uniform cumulative"
    (is (zero? (-> (opt-prng-xoro/make)
                   (sut/make 3 12)
                   (opt-distribution-prot/cumulative 3))))
    (is (= 0.5
           (-> (opt-prng-xoro/make)
               (sut/make 3 13)
               (opt-distribution-prot/cumulative 8))))
    (is (= 1.0
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               (opt-distribution-prot/cumulative 12)))))
  (testing (is (= 3
                  (-> (opt-prng-xoro/make)
                      (sut/make 3 12)
                      opt-distribution-prot/minimum))
               "minimum"))
  (is (= 12
         (-> (opt-prng-xoro/make)
             (sut/make 3 12)
             opt-distribution-prot/maximum))
      "maximum")
  (testing "quantile"
    (is (opt-maths/approx= 0.001
                           4.8
                           (-> (opt-prng-xoro/make)
                               (sut/make 3 12)
                               (opt-distribution-prot/quantile 0.2))))
    (is (opt-maths/approx= 0.001
                           7.5
                           (-> (opt-prng-xoro/make)
                               (sut/make 3 12)
                               (opt-distribution-prot/quantile 0.5)))))
  (is (= 4.5
         (-> (opt-prng-xoro/make)
             (sut/make 3 12)
             opt-distribution-prot/iqr))
      "Test uniform interquartile")
  (is (= {:iqr 4.5
          :min 3
          :q1 5.25
          :median 7.5
          :q3 9.75
          :max 12}
         (-> (opt-prng-xoro/make)
             (sut/make 3 12)
             opt-distribution-prot/summary))
      "Test uniform interquartile"))
