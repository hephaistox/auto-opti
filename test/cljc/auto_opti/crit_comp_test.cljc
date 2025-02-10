(ns auto-opti.crit-comp-test
  (:require
   [auto-opti.crit-comp :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest direct-eval-test
  (testing "crit-map"
    (is (neg? (sut/direct-eval {:crit-comp-name :crit-map
                                :crit-name :foo}
                               {:foo 12}
                               {:foo 14}))
        "first parameter is better.")
    (is (pos? (sut/direct-eval {:crit-comp-name :crit-map
                                :crit-name :foo}
                               {:foo 14}
                               {:foo 12}))
        "second parameter is better.")
    (is (zero? (sut/direct-eval {:crit-comp-name :crit-map
                                 :crit-name :foo}
                                {:foo 14}
                                {:foo 14}))
        "both parameters are equal.")
    (is (zero? (sut/direct-eval {:crit-comp-name :crit-map
                                 :crit-name :bar}
                                {:foo 12}
                                {:foo 14}))
        "nil values are ok."))
  (testing "Hierarchise"
    (is (neg? (sut/direct-eval {:crit-comp-name :hierarchise
                                :order [:a :b]}
                               {:a 12
                                :b 2
                                :c 100}
                               {:a 13
                                :b 2
                                :c 100}))
        "If crit1 is better on first criteria")
    (is (neg? (sut/direct-eval {:order [:b :a]
                                :crit-comp-name :hierarchise}
                               {:a 12
                                :b 2
                                :c 100}
                               {:a 13
                                :b 2
                                :c 100}))
        "If crit1 is better on second criteria")
    (is (pos? (sut/direct-eval {:order [:b :a]
                                :crit-comp-name :hierarchise}
                               {:a 13
                                :b 2
                                :c 100}
                               {:a 12
                                :b 2
                                :c 100}))
        "If crit2 is better on second criteria")
    (is (zero? (sut/direct-eval {:order [:b :a]
                                 :crit-comp-name :hierarchise}
                                {:a 13
                                 :b 2
                                 :c 100}
                                {:a 13
                                 :b 2
                                 :c 100}))
        "crit2=crit1"))
  (testing "weighted sum"
    (is (neg? (sut/direct-eval {:weights {:a 10
                                          :b 100}
                                :crit-comp-name :weighted-sum}
                               {:a 12
                                :b 2}
                               {:a 13
                                :b 4}))
        "weights makes crit1 better")
    (is (neg? (sut/direct-eval {:weights {:a 10
                                          :b 100}
                                :crit-comp-name :weighted-sum}
                               {:a 2
                                :b 2}
                               {:a 3
                                :b 3}))
        "weights makes crit1 better")
    (is (pos? (sut/direct-eval {:weights {:a -10
                                          :b -100}
                                :crit-comp-name :weighted-sum}
                               {:a 2
                                :b 2}
                               {:a 3
                                :b 3}))
        "weights makes crit2 better"))
  (testing "smaller"
    (is (neg? (sut/direct-eval {:crit-comp-name :smaller} 12 20)) "smaller is found")
    (is (zero? (sut/direct-eval {:crit-comp-name :smaller} 12 12)) "smaller can find equality")
    (is (pos? (sut/direct-eval {:crit-comp-name :smaller} 20 12)) "non smaller is found"))
  (testing "bigger"
    (is (pos? (sut/direct-eval {:crit-comp-name :bigger} 12 20)) "bigger is found")
    (is (zero? (sut/direct-eval {:crit-comp-name :bigger} 12 12)) "bigger can find equality")
    (is (neg? (sut/direct-eval {:crit-comp-name :bigger} 20 12)) "non bigger is found")))
