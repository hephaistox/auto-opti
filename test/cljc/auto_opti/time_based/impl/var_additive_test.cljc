(ns auto-opti.time-based.impl.var-additive-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.time-based.impl.storage-strategy.contiguous :as opt-tb-contiguous]
   [auto-opti.time-based.impl.storage-strategy.deltas     :as opt-tb-deltas]
   [auto-opti.time-based.impl.var-additive                :as sut]
   [auto-opti.time-based.protocol                         :as opt-tb-protocol]))

(deftest default-test
  (is (zero? (-> (sut/make (opt-tb-deltas/make))
                 opt-tb-protocol/default))))

(deftest get-measure-test
  (testing "Both storage implementation works the same."
    (is (= 3
           (-> (sut/make (opt-tb-deltas/make))
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/get-measure 10))
           (-> (sut/make (opt-tb-contiguous/make 14))
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/get-measure 10)))))
  (testing "Both storage implementation works the same, measures are added."
    (is (= 9
           (-> (sut/make (opt-tb-deltas/make))
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/get-measure 10))
           (-> (sut/make (opt-tb-contiguous/make 14))
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/measure 10 3)
               (opt-tb-protocol/get-measure 10))))))
