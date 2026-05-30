(ns auto-opti.crit-comp-test
  (:require
   [auto-opti           :as-alias opti]
   [auto-opti.crit-comp :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(defn- direct-eval
  "Helper to compare `crit1` and `crit2` based on map describing the `crit-comp-pars`. For test only, crit-comp-fn once the crit-comp and use it multiple times instead."
  [crit-comp-pars crit1 crit2]
  ((-> crit-comp-pars
       sut/crit-comp-fn)
   crit1
   crit2))

(deftest direct-eval-test
  (testing "Hierarchise"
    (is (= :better
           (direct-eval #::opti{:crit-comp-name :hierarchise
                                :order [#::opti{:crit-comp-name :smaller
                                                :crit-name :a}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :b}]}
                        {:a 12
                         :b 2
                         :c 100}
                        {:a 13
                         :b 2
                         :c 100}))
        "If crit1 is better on first criteria")
    (is (= :better
           (direct-eval #::opti{:order [#::opti{:crit-comp-name :smaller
                                                :crit-name :b}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :a}]
                                :crit-comp-name :hierarchise}
                        {:a 12
                         :b 2
                         :c 100}
                        {:a 13
                         :b 2
                         :c 100}))
        "If crit1 is better on second criteria")
    (is (= :worst
           (direct-eval #::opti{:order [#::opti{:crit-comp-name :smaller
                                                :crit-name :b}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :a}]
                                :crit-comp-name :hierarchise}
                        {:a 13
                         :b 2
                         :c 100}
                        {:a 12
                         :b 2
                         :c 100}))
        "If crit2 is better on second criteria")
    (is (= :equal
           (direct-eval #::opti{:order [#::opti{:crit-comp-name :smaller
                                                :crit-name :b}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :a}]
                                :crit-comp-name :hierarchise}
                        {:a 13
                         :b 2
                         :c 100}
                        {:a 13
                         :b 2
                         :c 100}))
        "crit2=crit1")
    (is (= :better
           (direct-eval #::opti{:order [#::opti{:crit-comp-name :smaller
                                                :crit-name :b}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :a}
                                        #::opti{:crit-comp-name :smaller
                                                :crit-name :c}]
                                :crit-comp-name :hierarchise}
                        {:a 13
                         :b 2
                         :c 100}
                        {:a 13
                         :b 2
                         :c 101}))
        "Last criteria is examined")
    (is (nil? (direct-eval #::opti{:order [#::opti{:crit-comp-name :smaller
                                                   :crit-name :b}
                                           #::opti{:crit-comp-name :non-existing
                                                   :crit-name :a}
                                           #::opti{:crit-comp-name :smaller
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
    (is (= :better
           (direct-eval #::opti{:weights {:a 10
                                          :b 100}
                                :crit-comp-name :weighted-sum}
                        {:a 12
                         :b 2}
                        {:a 13
                         :b 4}))
        "weights makes crit1 better")
    (is (= :better
           (direct-eval #::opti{:weights {:a 10
                                          :b 100}
                                :crit-comp-name :weighted-sum}
                        {:a 2
                         :b 2}
                        {:a 3
                         :b 3}))
        "weights makes crit1 better")
    (is (= :worst
           (direct-eval #::opti{:weights {:a -10
                                          :b -100}
                                :crit-comp-name :weighted-sum}
                        {:a 2
                         :b 2}
                        {:a 3
                         :b 3}))
        "weights makes crit2 better")
    (testing "Missing weight are ignored"
      (is (= :equal
             (direct-eval #::opti{:weights {:a -10}
                                  :crit-comp-name :weighted-sum}
                          {:a 2
                           :b 2}
                          {:a 2
                           :b 3})))
      (is (= :equal
             (direct-eval #::opti{:weights {:a -10
                                            :b nil}
                                  :crit-comp-name :weighted-sum}
                          {:a 2
                           :b 2}
                          {:a 2
                           :b 3})))))
  (testing "Strict"
    (is (= :equal
           (direct-eval #::opti{:crit-comp-name :strict
                                :crit-names [:a :b]}
                        {:a 10
                         :b 15
                         :c 25}
                        {:a 10
                         :b 15
                         :c 25}))
        "Equality")
    (is (= :worst
           (direct-eval #::opti{:crit-comp-name :strict
                                :crit-names [:b :a]}
                        {:a 11
                         :b 15
                         :c 25}
                        {:a 10
                         :b 15
                         :c 25}))
        "Worst")
    (is (= :better
           (direct-eval #::opti{:crit-comp-name :strict
                                :crit-names [:b :a]}
                        {:a 9
                         :b 15
                         :c 25}
                        {:a 10
                         :b 15
                         :c 25}))
        "Better")
    (is (= :crit-nc
           (direct-eval #::opti{:crit-comp-name :strict
                                :crit-names [:b :a]}
                        {:a 11
                         :b 15
                         :c 25}
                        {:a 10
                         :b 16
                         :c 25}))
        "Non comparable"))
  (testing "smaller"
    (is (= :better (direct-eval #::opti{:crit-comp-name :smaller} 12 20)) "crit1 is smaller")
    (is (= :equal (direct-eval #::opti{:crit-comp-name :smaller} 12 12)) "criteria are equal")
    (is (= :worst (direct-eval #::opti{:crit-comp-name :smaller} 20 12)) "crit2 is smaller")
    (is (= :nc (direct-eval #::opti{:crit-comp-name :smaller} nil 12))
        "nil crit1 is not comparable")
    (is (= :nc (direct-eval #::opti{:crit-comp-name :smaller} 12 nil))
        "nil crit2 is not comparable")
    (is (= :nc (direct-eval #::opti{:crit-comp-name :smaller} nil nil))
        "two nil are not comparable"))
  (testing "bigger"
    (is (= :worst (direct-eval #::opti{:crit-comp-name :bigger} 12 20)) "crit2 is bigger")
    (is (= :equal (direct-eval #::opti{:crit-comp-name :bigger} 12 12)) "criteria are equal")
    (is (= :better (direct-eval #::opti{:crit-comp-name :bigger} 20 12)) "crit1 is bigger")
    (is (= :nc (direct-eval #::opti{:crit-comp-name :bigger} nil 12)) "nil crit1 is not comparable")
    (is (= :nc (direct-eval #::opti{:crit-comp-name :bigger} 12 nil))
        "nil crit2 is not comparable")))

(deftest crit-comp-fn-test
  (testing "An unknown crit-comp-name yields no comparator."
    (is (nil? (sut/crit-comp-fn #::opti{:crit-comp-name :does-not-exist}))))
  (testing "An explicit empty registry yields no comparator even for a known name."
    (is (nil? (sut/crit-comp-fn #::opti{:registry {}
                                        :crit-comp-name :smaller}))))
  (testing "The default registry validates against the registry schema names being keywords."
    (is (every? keyword? (keys sut/default-registry)))))
