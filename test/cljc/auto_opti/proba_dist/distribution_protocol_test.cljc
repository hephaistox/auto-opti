(ns auto-opti.proba-dist.distribution-protocol-test
  (:require
   [auto-opti.prng.impl.xoroshiro128           :as opt-prng-xoro]
   #?@(:clj [[clojure.test :refer [deftest is]]]
       :cljs [[cljs.test :refer [deftest is] :include-macros true]])
   [auto-opti.proba-dist.distribution-protocol :as sut]
   [auto-opti.proba-dist.impl.uniform          :as opt-uniform]))

(deftest iqr-test
  (is (= 4.0
         (-> (opt-prng-xoro/make)
             (opt-uniform/make 7 15)
             sut/iqr))
      "Inter quartile of uniform distribution is ok."))

(deftest summary-test
  (is (= {:min 1
          :q1 25.75
          :median 50.5
          :q3 75.25
          :max 100
          :iqr 49.5}
         (-> (opt-prng-xoro/make)
             (opt-uniform/make 1 100)
             sut/summary))
      "Summary of uniform distribution is ok."))

(deftest critical-value-test
  (is (= 97.5
         (-> (opt-prng-xoro/make)
             (opt-uniform/make 0 100)
             sut/critical-value))
      "Critical value of uniform distribution is ok."))
