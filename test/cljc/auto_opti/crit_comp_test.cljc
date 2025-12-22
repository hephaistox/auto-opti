(ns auto-opti.crit-comp-test
  (:require
   [auto-opti.crit-comp :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest direct-eval-test
  (testing "Hierarchise"
    (is (= :crit1-better
           (sut/direct-eval {:crit-comp-name :hierarchise
                             :order [{:crit-comp-name :smaller
                                      :crit-name :a}
                                     {:crit-comp-name :smaller
                                      :crit-name :b}]}
                            {:a 12
                             :b 2
                             :c 100}
                            {:a 13
                             :b 2
                             :c 100}))
        "If crit1 is better on first criteria")
    (is (= :crit1-better
           (sut/direct-eval {:order [{:crit-comp-name :smaller
                                      :crit-name :b}
                                     {:crit-comp-name :smaller
                                      :crit-name :a}]
                             :crit-comp-name :hierarchise}
                            {:a 12
                             :b 2
                             :c 100}
                            {:a 13
                             :b 2
                             :c 100}))
        "If crit1 is better on second criteria")
    (is (= :crit1-worst
           (sut/direct-eval {:order [{:crit-comp-name :smaller
                                      :crit-name :b}
                                     {:crit-comp-name :smaller
                                      :crit-name :a}]
                             :crit-comp-name :hierarchise}
                            {:a 13
                             :b 2
                             :c 100}
                            {:a 12
                             :b 2
                             :c 100}))
        "If crit2 is better on second criteria")
    (is (= :crit-eq
           (sut/direct-eval {:order [{:crit-comp-name :smaller
                                      :crit-name :b}
                                     {:crit-comp-name :smaller
                                      :crit-name :a}]
                             :crit-comp-name :hierarchise}
                            {:a 13
                             :b 2
                             :c 100}
                            {:a 13
                             :b 2
                             :c 100}))
        "crit2=crit1")
    (is (= :crit1-better
           (sut/direct-eval {:order [{:crit-comp-name :smaller
                                      :crit-name :b}
                                     {:crit-comp-name :smaller
                                      :crit-name :a}
                                     {:crit-comp-name :smaller
                                      :crit-name :c}]
                             :crit-comp-name :hierarchise}
                            {:a 13
                             :b 2
                             :c 100}
                            {:a 13
                             :b 2
                             :c 101}))
        "Last criteria is examined")
    (is (nil? (sut/direct-eval {:order [{:crit-comp-name :smaller
                                         :crit-name :b}
                                        {:crit-comp-name :non-existing
                                         :crit-name :a}
                                        {:crit-comp-name :smaller
                                         :crit-name :c}]
                                :crit-comp-name :hierarchise}
                               {:a 13
                                :b 2
                                :c 100}
                               {:a 13
                                :b 2
                                :c 100}))
        "Non existing criteria is skipped"))
  (testing "weighted sum"
    (is (= :crit1-better
           (sut/direct-eval {:weights {:a 10
                                       :b 100}
                             :crit-comp-name :weighted-sum}
                            {:a 12
                             :b 2}
                            {:a 13
                             :b 4}))
        "weights makes crit1 better")
    (is (= :crit1-better
           (sut/direct-eval {:weights {:a 10
                                       :b 100}
                             :crit-comp-name :weighted-sum}
                            {:a 2
                             :b 2}
                            {:a 3
                             :b 3}))
        "weights makes crit1 better")
    (is (= :crit1-worst
           (sut/direct-eval {:weights {:a -10
                                       :b -100}
                             :crit-comp-name :weighted-sum}
                            {:a 2
                             :b 2}
                            {:a 3
                             :b 3}))
        "weights makes crit2 better")
    (testing "Missing weight are ignored"
      (is (= :crit-eq
             (sut/direct-eval {:weights {:a -10}
                               :crit-comp-name :weighted-sum}
                              {:a 2
                               :b 2}
                              {:a 2
                               :b 3})))
      (is (= :crit-eq
             (sut/direct-eval {:weights {:a -10
                                         :b nil}
                               :crit-comp-name :weighted-sum}
                              {:a 2
                               :b 2}
                              {:a 2
                               :b 3})))))
  (testing "Strict"
    (is (= :crit-eq
           (sut/direct-eval {:crit-comp-name :strict
                             :crit-names [:a :b]}
                            {:a 10
                             :b 15
                             :c 25}
                            {:a 10
                             :b 15
                             :c 25}))
        "Equality")
    (is (= :crit1-worst
           (sut/direct-eval {:crit-comp-name :strict
                             :crit-names [:b :a]}
                            {:a 11
                             :b 15
                             :c 25}
                            {:a 10
                             :b 15
                             :c 25}))
        "Worst")
    (is (= :crit1-better
           (sut/direct-eval {:crit-comp-name :strict
                             :crit-names [:b :a]}
                            {:a 9
                             :b 15
                             :c 25}
                            {:a 10
                             :b 15
                             :c 25}))
        "Better")
    (is (= :crit-nc
           (sut/direct-eval {:crit-comp-name :strict
                             :crit-names [:b :a]}
                            {:a 11
                             :b 15
                             :c 25}
                            {:a 10
                             :b 16
                             :c 25}))
        "Non comparable"))
  (testing "smaller"
    (is (= :crit1-better (sut/direct-eval {:crit-comp-name :smaller} 12 20)) "crit1 is smaller")
    (is (= :crit-eq (sut/direct-eval {:crit-comp-name :smaller} 12 12)) "criteria are equal")
    (is (= :crit1-worst (sut/direct-eval {:crit-comp-name :smaller} 20 12)) "crit2 is smaller"))
  (testing "bigger"
    (is (= :crit1-worst (sut/direct-eval {:crit-comp-name :bigger} 12 20)) "crit2 is bigger")
    (is (= :crit-eq (sut/direct-eval {:crit-comp-name :bigger} 12 12)) "criteria are equal")
    (is (= :crit1-better (sut/direct-eval {:crit-comp-name :bigger} 20 12)) "crit1 is bigger")))
