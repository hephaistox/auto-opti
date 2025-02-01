(ns auto-opti.crit-comp.registry-test
  (:require
   [auto-core.schema             :as core-schema]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.crit-comp.registry :as sut]))

(deftest id-test (is (nil? (core-schema/validate-humanize sut/id)) "id is a valid schema"))

(deftest schema-test (is (nil? (core-schema/validate-humanize sut/schema)) "schema is valid"))

(deftest registry-test
  (is (nil? (core-schema/validate-data-humanize sut/schema sut/registry)) "registry is valid")
  (testing "crit registry entry"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry [:crit-map :params-schema])
                                                  {:crit-name :arg}))
        "Parameters comply their schema")
    (is (neg? (((get-in sut/registry [:crit-map :f]) {:crit-name :foo}) {:foo 12} {:foo 200}))
        "`crit1` is better")
    (is (pos? (((get-in sut/registry [:crit-map :f]) {:crit-name :foo}) {:foo 200} {:foo 12}))
        "`crit2` is better")
    (is (zero? (((get-in sut/registry [:crit-map :f]) {}) {:foo 12} {:foo 200}))
        "No crit-name defined means `crit1=crit2`"))
  (testing "hierarchise registry entry"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry
                                                          [:hierarchise :params-schema])
                                                  {:order []}))
        "Parameters comply their schema")
    (is (neg? (((get-in sut/registry [:hierarchise :f]) {:order [:foo :bar]})
               {:foo 2
                :bar 0}
               {:foo 2
                :bar 1}))
        "`crit1` is better")
    (is (pos? (((get-in sut/registry [:hierarchise :f]) {:order [:foo :bar]})
               {:foo 2
                :bar 3}
               {:foo 2
                :bar 1}))
        "`crit2` is better")
    (is (zero? (((get-in sut/registry [:hierarchise :f]) {:order []})
                {:foo 200
                 :bar 120}
                {:foo 200
                 :bar 120}))
        "No order means `crit1=crit2`"))
  (testing "weighted sum"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry
                                                          [:weighted-sum :params-schema])
                                                  {:weights {:foo 10
                                                             :bar 14}}))
        "Parameters comply their schema")
    (is (neg? (((get-in sut/registry [:weighted-sum :f])
                {:weights {:foo 10
                           :bar 14}})
               {:foo 2
                :bar 0}
               {:foo 2
                :bar 1}))
        "`crit1` is better")
    (is (pos? (((get-in sut/registry [:weighted-sum :f])
                {:weights {:foo 10
                           :bar 14}})
               {:foo 2
                :bar 3}
               {:foo 2
                :bar 1}))
        "`crit2` is better")
    (is (zero? (((get-in sut/registry [:weighted-sum :f]) {})
                {:foo 210
                 :bar 120}
                {:foo 230
                 :bar 140}))
        "No weights means `crit1=crit2`"))
  (testing "smaller"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry [:smaller :params-schema])
                                                  nil))
        "Parameters comply their schema")
    (is (neg? (((get-in sut/registry [:smaller :f]) nil) 0 10)) "`crit1` is better")
    (is (pos? (((get-in sut/registry [:smaller :f]) nil) 10 0)) "`crit2` is better")
    (is (zero? (((get-in sut/registry [:smaller :f]) {}) 200 200)) "No order means `crit1=crit2`"))
  (testing "bigger"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry [:bigger :params-schema])
                                                  nil))
        "Parameters comply their schema")
    (is (neg? (((get-in sut/registry [:bigger :f]) nil) 10 0)) "`crit1` is better")
    (is (pos? (((get-in sut/registry [:bigger :f]) nil) 0 10)) "`crit2` is better")
    (is (zero? (((get-in sut/registry [:bigger :f]) {}) 200 200)) "No order means `crit1=crit2`"))
  (testing "kpi compare"
    (is (nil? (core-schema/validate-data-humanize (get-in sut/registry [:strict :params-schema])
                                                  {:crit-names [:foo :bar]}))
        "Parameters comply their schema")
    (is (= :crit1-better
           (((get-in sut/registry [:strict :f]) {:crit-names [:foo :bar]})
            {:foo 10
             :bar 12}
            {:foo 10
             :bar 14}))
        "`crit1` is better")
    (is (= :crit1-worst
           (((get-in sut/registry [:strict :f]) {:crit-names [:foo :bar]})
            {:foo 10
             :bar 19}
            {:foo 10
             :bar 14}))
        "`crit2` is better")
    (is (= :eq
           (((get-in sut/registry [:strict :f]) {:crit-names [:foo :bar]})
            {:foo 10
             :bar 14}
            {:foo 10
             :bar 14}))
        "`crit1` and `crit2` are equal")
    (is (= :nc
           (((get-in sut/registry [:strict :f]) {:crit-names [:foo :bar]})
            {:foo 10
             :bar 14}
            {:foo 14
             :bar 10}))
        "`crit1` and `crit2` are not comparable")
    (is (= :eq (((get-in sut/registry [:strict :f]) {:crit-names []}) {:a :b} {:c :d}))
        "No order means `crit1=crit2`")))
