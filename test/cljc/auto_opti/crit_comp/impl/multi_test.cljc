(ns auto-opti.crit-comp.impl.multi-test
  (:require
   [auto-opti.crit-comp.impl.multi :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest strict-test
  (is (= :crit1-better (let [f (sut/strict [:foo])] (f {:foo 12} {:foo 14}))) "a <= b true")
  (is (= :crit1-worst (let [f (sut/strict [:foo])] (f {:foo 14} {:foo 12}))) "a <= b false")
  (is (= :eq (let [f (sut/strict [:foo])] (f {:foo 12} {:foo 12}))) "a == b true")
  (testing "a<=b"
    (is (= :crit1-better
           (let [f (sut/strict [:foo :bar])]
             (f {:foo 12
                 :bar 10}
                {:foo 14
                 :bar 11})))
        "all criteria are strictly")
    (is (= :crit1-better
           (let [f (sut/strict [:foo :bar])]
             (f {:foo 12
                 :bar 10}
                {:foo 14
                 :bar 10})))
        "some criteria are equal, some are strictly")
    (testing "a == b"
      (is (= :eq
             (let [f (sut/strict [:foo :bar])]
               (f {:foo 12
                   :bar 10}
                  {:foo 12
                   :bar 10})))
          "a == b true"))))
