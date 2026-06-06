(ns auto-opti.tb-var.impl.storage-strategy-test
  "Tests for the shared `BucketData` helpers, here the `occupation-rate`.

  The `occupation-rate` is exercised against both storage strategies to check the
  protocol dispatch works uniformly."
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.tb-var.impl.storage-strategy            :as sut]
   [auto-opti.tb-var.impl.storage-strategy.contiguous :as opt-tb-contiguous]
   [auto-opti.tb-var.impl.storage-strategy.deltas     :as opt-tb-deltas]))

(deftest occupation-rate-deltas-test
  (testing "In a deltas storage capacity grows with the data, so every slot is occupied."
    ;; nb-set and capacity are both the count of stored deltas, hence a full rate of 1.
    (is (= (sut/nb-set (sut/assoc-date (opt-tb-deltas/make) 3 :v))
           (sut/capacity (sut/assoc-date (opt-tb-deltas/make) 3 :v)))))
  (testing "In a deltas storage every slot is occupied, so the rate is always 1."
    (is (= 1
           (-> (opt-tb-deltas/make)
               (sut/assoc-date 3 :v)
               sut/occupation-rate)))
    (is (= 1
           (-> (opt-tb-deltas/make)
               (sut/assoc-date 3 :v)
               (sut/assoc-date 7 :v)
               sut/occupation-rate)))))

(deftest occupation-rate-contiguous-test
  (testing "A fresh contiguous storage is empty."
    (is (zero? (sut/occupation-rate (opt-tb-contiguous/make 10)))))
  (testing "The rate reflects the number of set buckets over the capacity."
    (is (= #?(:clj 1/5
              :cljs 0.2)
           (-> (opt-tb-contiguous/make 5)
               (sut/assoc-date 0 :v)
               sut/occupation-rate)))))
