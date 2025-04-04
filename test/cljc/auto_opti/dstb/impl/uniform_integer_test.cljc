(ns auto-opti.dstb.impl.uniform-integer-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.dstb.dstb-protocol        :as opt-dstb-prot]
   [auto-opti.dstb.impl.uniform-integer :as sut]
   [auto-opti.maths                     :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128    :as opt-prng-xoro]))

(deftest uniform-test
  (testing "Uniform distribution returns elements in the range"
    (is (every? int?
                (repeatedly 10
                            (fn []
                              (-> (opt-prng-xoro/make)
                                  (sut/make 2 60)
                                  opt-dstb-prot/draw))))))
  (testing "Uniform distribution returns elements in the range"
    (is (= 31
           (-> (opt-prng-xoro/make)
               (sut/make 2 60)
               opt-dstb-prot/median)))
    (is (= #?(:clj 5/2
              :cljs 2.5)
           (-> (opt-prng-xoro/make)
               (sut/make 2 3)
               opt-dstb-prot/median))))
  (testing "Test uniform cumulative"
    (is (zero? (-> (opt-prng-xoro/make)
                   (sut/make 3 12)
                   (opt-dstb-prot/cumulative 3))))
    (is (= #?(:clj 1/2
              :cljs 0.5)
           (-> (opt-prng-xoro/make)
               (sut/make 3 13)
               (opt-dstb-prot/cumulative 8))))
    (is (= 1.0
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               (opt-dstb-prot/cumulative 12)))))
  (testing "minimum"
    (is (= 3
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               opt-dstb-prot/minimum))))
  (testing "maximum"
    (is (= 12
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               opt-dstb-prot/maximum))))
  (testing "quantile"
    (is (opt-maths/approx= 4.8
                           (-> (opt-prng-xoro/make)
                               (sut/make 3 12)
                               (opt-dstb-prot/quantile 0.2))
                           0.001))
    (is (opt-maths/approx= 7.5
                           (-> (opt-prng-xoro/make)
                               (sut/make 3 12)
                               (opt-dstb-prot/quantile 0.5))
                           0.001)))
  (testing "Test uniform interquartile"
    (is (= 4.5
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               opt-dstb-prot/iqr))))
  (testing "Test uniform interquartile"
    (is (= {:iqr 4.5
            :min 3
            :q1 5.25
            :median 7.5
            :q3 9.75
            :max 12}
           (-> (opt-prng-xoro/make)
               (sut/make 3 12)
               opt-dstb-prot/summary)))))
