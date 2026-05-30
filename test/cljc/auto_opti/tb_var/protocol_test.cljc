(ns auto-opti.tb-var.protocol-test
  "Exercises the `TimeBased` protocol against a concrete implementation."
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.tb-var.impl.storage-strategy.deltas :as opt-tb-deltas]
   [auto-opti.tb-var.impl.var-latest              :as opt-tb-var-latest]
   [auto-opti.tb-var.protocol                     :as sut]))

(defn- new-tb-var [] (opt-tb-var-latest/make (opt-tb-deltas/make) :default))

(deftest default-test
  (testing "`default` returns the default value before any measurement."
    (is (= :default (sut/default (new-tb-var))))))

(deftest measure-and-get-measure-test
  (testing "`measure` returns a new TimeBased instance carrying the data."
    (let [tb (sut/measure (new-tb-var) 10 :v)]
      (is (not (identical? tb (new-tb-var))) "A fresh instance is returned")
      (is (= :v (sut/get-measure tb 10)))))
  (testing "`get-measure` falls back to the default before the first measure."
    (is (= :default (sut/get-measure (new-tb-var) 10)))))

(deftest get-measures-test
  (testing "`get-measures` returns one value per requested bucket."
    (is (= [:default :default :v :v]
           (-> (new-tb-var)
               (sut/measure 10 :v)
               (sut/get-measures (range 8 12)))))))
