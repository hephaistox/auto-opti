(ns auto-opti.sample.vector-test
  (:require
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])
   [auto-opti.maths                  :as opt-maths]
   [auto-opti.sample.impl.vector     :as sut]
   [auto-opti.sample.sample-protocol :as opt-sample-prot]))

(deftest SampleVector-test
  (testing "Sample data vector is running"
    (is (= 2
           (-> (sut/make [:a :b])
               opt-sample-prot/n)))
    (is (= 5
           (-> (sut/make [4 6])
               opt-sample-prot/average)))
    (is (opt-maths/approx= 1
                           (-> (sut/make [4 6])
                               opt-sample-prot/variance)
                           0.0001))
    (is (= 5
           (-> (sut/make [4 6])
               opt-sample-prot/median)))))
