(ns auto-opti.dstb.impl.exponential-test
  (:require
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])
   [auto-opti.dstb                   :as opt-dstb]
   [auto-opti.dstb.impl.exponential  :as sut]
   [auto-opti.maths                  :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128 :as opt-prng-xoro]))

(def prng-stub (opt-prng-xoro/make))

(deftest draw-test
  (testing "Test for draw"
    (is (double? (-> (sut/make prng-stub 2.0)
                     opt-dstb/draw)))))

(deftest median-test
  (testing "Is the median of exponential 2 is (ln 2)/lambda"
    (is (opt-maths/approx= (-> (sut/make prng-stub 2.0)
                               opt-dstb/median)
                           0.34657
                           0.00001))))

(deftest cumulative-test
  (testing "Cumulative"
    (is (opt-maths/approx= (opt-dstb/cumulative (sut/make prng-stub 2.0) 0.4) 0.5506 0.0001))))

(deftest minimun-test (testing "Minimum" (is (zero? (opt-dstb/minimum (sut/make prng-stub 2.0))))))

(deftest maximun-test
  (testing "Maximum" (is (opt-maths/infinite? (opt-dstb/maximum (sut/make prng-stub 2.0))))))

(deftest quantile-test
  (testing "Quantile" (is (double? (opt-dstb/quantile (sut/make prng-stub 2.0) 0.3)))))
