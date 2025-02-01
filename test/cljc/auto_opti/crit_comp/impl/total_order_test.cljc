(ns auto-opti.crit-comp.impl.total-order-test
  (:require
   [auto-opti.crit-comp.impl.total-order :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest crit-map-test
  (is (neg? (let [f (sut/crit-map :foo)] (f {:foo 12} {:foo 14}))) "first parameter is better.")
  (is (pos? (let [f (sut/crit-map :foo)] (f {:foo 14} {:foo 12}))) "second parameter is better.")
  (is (zero? (let [f (sut/crit-map :foo)] (f {:foo 14} {:foo 14}))) "both parameters are equal.")
  (is (zero? (let [f (sut/crit-map :bar)] (f {:foo 12} {:foo 14}))) "nil values are ok."))

(deftest hierarchise-test
  (is (neg? (let [f (sut/hierarchise [:a :b])]
              (f {:a 12
                  :b 2
                  :c 100}
                 {:a 13
                  :b 2
                  :c 100})))
      "If crit1 is better on first criteria")
  (is (neg? (let [f (sut/hierarchise [:b :a])]
              (f {:a 12
                  :b 2
                  :c 100}
                 {:a 13
                  :b 2
                  :c 100})))
      "If crit1 is better on second criteria")
  (is (pos? (let [f (sut/hierarchise [:b :a])]
              (f {:a 13
                  :b 2
                  :c 100}
                 {:a 12
                  :b 2
                  :c 100})))
      "If crit2 is better on second criteria")
  (is (zero? (let [f (sut/hierarchise [:b :a])]
               (f {:a 13
                   :b 2
                   :c 100}
                  {:a 13
                   :b 2
                   :c 100})))
      "crit2=crit1"))

(deftest weighted-sum-test
  (is (neg? (let [f (sut/weighted-sum-comp {:a 10
                                            :b 100})]
              (f {:a 12
                  :b 2}
                 {:a 13
                  :b 4})))
      "weights makes crit1 better")
  (is (neg? (let [f (sut/weighted-sum-comp {:a 10
                                            :b 100})]
              (f {:a 2
                  :b 2}
                 {:a 3
                  :b 3})))
      "weights makes crit1 better")
  (is (pos? (let [f (sut/weighted-sum-comp {:a -10
                                            :b -100})]
              (f {:a 2
                  :b 2}
                 {:a 3
                  :b 3})))
      "weights makes crit2 better"))

(deftest smaller-test
  (is (neg? (let [f (sut/smaller-comp)] (f 12 20))) "smaller is found")
  (is (zero? (let [f (sut/smaller-comp)] (f 12 12))) "smaller can find equality")
  (is (pos? (let [f (sut/smaller-comp)] (f 20 12))) "non smaller is found"))

(deftest bigger-test
  (is (pos? (let [f (sut/bigger-comp)] (f 12 20))) "bigger is found")
  (is (zero? (let [f (sut/bigger-comp)] (f 12 12))) "bigger can find equality")
  (is (neg? (let [f (sut/bigger-comp)] (f 20 12))) "non bigger is found"))
