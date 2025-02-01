(ns auto-opti.time-based.impl.var-latest-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.time-based.impl.storage-strategy.contiguous :as opt-tb-contiguous]
   [auto-opti.time-based.impl.storage-strategy.deltas     :as opt-tb-deltas]
   [auto-opti.time-based.impl.var-latest                  :as sut]
   [auto-opti.time-based.protocol                         :as opt-tb-protocol]))

(deftest default-test
  (is (= 10
         (-> (sut/make (opt-tb-deltas/make) 10)
             opt-tb-protocol/default))))

(deftest get-measure-test
  (testing "get-measure works the same for both storage implementation"
    (is (= :v
           (-> (sut/make (opt-tb-deltas/make) 10)
               (opt-tb-protocol/measure 10 :v)
               (opt-tb-protocol/get-measure 10))
           (-> (sut/make (opt-tb-contiguous/make 10) 10)
               (opt-tb-protocol/measure 10 :v)
               (opt-tb-protocol/get-measure 10))))))

(deftest get-measures-test
  (testing "When :v is set at 10, all following dates are returning :v"
    (is (= [0 0 :v :v :v :v]
           (-> (sut/make (opt-tb-deltas/make) 0)
               (opt-tb-protocol/measure 10 :v)
               (opt-tb-protocol/get-measures (range 8 14)))
           (-> (sut/make (opt-tb-contiguous/make 10) 0)
               (opt-tb-protocol/measure 10 :v)
               (opt-tb-protocol/get-measures (range 8 14)))))))
