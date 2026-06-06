(ns auto-opti.tb-var.impl.var-aggregated-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti                                     :as-alias opti]
   [auto-opti.tb-var.impl.aggregates              :as opt-tb-aggregates]
   [auto-opti.tb-var.impl.storage-strategy.deltas :as opt-tb-deltas]
   [auto-opti.tb-var.impl.var-additive            :as opt-tb-var-additive]
   [auto-opti.tb-var.impl.var-aggregated          :as sut]
   [auto-opti.tb-var.protocol                     :as opt-tb-protocol]))

(defn- aggregator
  "Aggregator gathering buckets [10;20[ into aggregates of `step` 5."
  []
  (opt-tb-aggregates/make-aggregator [#::opti{:start-bucket 10
                                              :end-bucket 20
                                              :step 5}]))

(defn- make-aggregated [] (sut/make (opt-tb-var-additive/make (opt-tb-deltas/make)) (aggregator)))

(deftest make-test
  (testing "`make` builds a TimeBased delegating to an inner tb-var through an aggregator."
    (let [tb (make-aggregated)] (is (satisfies? opt-tb-protocol/TimeBased tb)))))

(deftest measure-and-get-measure-test
  (testing "Buckets inside the same aggregate share the aggregated value."
    (let [tb (-> (make-aggregated)
                 (opt-tb-protocol/measure 10 3)
                 (opt-tb-protocol/measure 14 4))]
      (is (= 7 (opt-tb-protocol/get-measure tb 10)) "10 maps to aggregate 0")
      (is (= 7 (opt-tb-protocol/get-measure tb 11)) "11 maps to the same aggregate 0")
      (is (= 0 (opt-tb-protocol/get-measure tb 15)) "15 maps to aggregate 1, untouched")))
  (testing "A bucket outside any aggregate is a no-op on measure and returns nil on read."
    (let [tb (make-aggregated)]
      (is (nil? (opt-tb-protocol/get-measure tb 5)) "Bucket below the first aggregate")
      (is (identical? tb (opt-tb-protocol/measure tb 5 99))
          "Measuring an out-of-range bucket returns the unchanged instance"))))

(deftest get-measures-test
  (testing "`get-measures` returns one value per distinct aggregate covered by the buckets."
    (let [tb (-> (make-aggregated)
                 (opt-tb-protocol/measure 10 3)
                 (opt-tb-protocol/measure 14 4))]
      ;; buckets 8 9 (outside) are dropped, 10-19 collapse into aggregates 0 and 1.
      (is (= [7 0] (opt-tb-protocol/get-measures tb (range 8 20)))))))
