(ns auto-opti.prng.impl.tests-test
  (:require
   [auto-opti.prng.impl.tests :as sut]
   #?@(:clj [[clojure.test :refer [deftest is testing]]]
       :cljs [[cljs.test :refer [deftest is testing] :include-macros true]])))

(deftest dstb-uniformity-test
  (testing "Accept regular ones"
    (is (empty? (sut/dstb-uniformity (-> (concat (repeat 30 0) (repeat 30 1))
                                         shuffle)
                                     0.0001))))
  (testing "Refuse empty maps"
    (is (empty? (sut/dstb-uniformity (-> (concat (repeat 4 0) (repeat 30 1))
                                         shuffle)
                                     77)))))
