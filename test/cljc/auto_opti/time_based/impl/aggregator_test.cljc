(ns auto-opti.time-based.impl.aggregator-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.time-based                 :as opt-tb]
   [auto-opti.time-based.impl.aggregates :as opt-tb-aggregates]
   [auto-opti.time-based.impl.aggregator :as sut]))

(deftest bucket-aggregate-test
  (is (= [nil 0 5 94 nil nil nil]
         (mapv #(sut/bucket-aggregate [#::opt-tb{:start-bucket 5
                                                 :step 1
                                                 :end-bucket 100
                                                 :start-bucket-aggregate 0}]
                                      %)
               [4 5 10 99 100 101 10000]))
      "Single aggregate in the aggregates is working.")
  (is (= [nil 0 4 nil nil nil 5 9 nil nil]
         (mapv #(sut/bucket-aggregate [#::opt-tb{:start-bucket 5
                                                 :end-bucket 10
                                                 :step 1
                                                 :end-bucket-aggregate 5
                                                 :start-bucket-aggregate 0}
                                       #::opt-tb{:start-bucket 15
                                                 :end-bucket 20
                                                 :step 1
                                                 :start-bucket-aggregate 5}]
                                      %)
               [4 5 9 10 11 14 15 19 20 21]))
      "Multiple aggregate in the aggregates is working."))

(deftest bucket-aggregates-test
  (is
   (= [0 1 2 3 4 5 6 7 12]
      (-> [#::opt-tb{:start-bucket 2
                     :end-bucket 5
                     :step 1
                     :start-bucket-aggregate 0
                     :end-bucket-aggregate 3}
           #::opt-tb{:start-bucket 5
                     :end-bucket 25
                     :step 2
                     :start-bucket-aggregate 3
                     :end-bucket-aggregate 13}]
          (sut/bucket-aggregates [1 2 2 3 4 5 6 7 8 9 10 11 12 13 14 24 25 26 100])))
   "`bucket`s are translated, the ones outsides the aggregates are forgotten, when many buckets lead to the same bucket-aggregate so it is returned once."))

(deftest bucket-range-test
  (is (= [3 4]
         (-> (opt-tb-aggregates/make-aggregator [#::opt-tb{:start-bucket 0}
                                                 #::opt-tb{:start-bucket 5
                                                           :step 2}])
             (sut/bucket-range 3)))
      "A simple bucket-aggregate is translated."))
