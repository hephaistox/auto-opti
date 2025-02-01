(ns auto-opti.time-based-test
  "See `time_based.md` for explanations on the tests."
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.time-based :as sut]))

(deftest tb-var-additive-composition-test
  (is (= 0
         (-> (sut/tb-var-additive-deltas)
             (sut/get-measure 10))))
  (is (= -666
         (-> (sut/tb-var-additive-deltas -666)
             (sut/get-measure 10))))
  (is (= 3
         (-> (sut/tb-var-additive-deltas 3)
             sut/default)))
  (is (= 3
         (-> (sut/tb-var-latest-deltas)
             (sut/measure 10 3)
             (sut/get-measure 10))))
  (is (= 5
         (-> (sut/tb-var-additive-deltas)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/measure 11 4)
             (sut/get-measure 10))))
  (is (= [0 0 5 4 0 0]
         (-> (sut/tb-var-additive-deltas)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/measure 11 4)
             (sut/get-measures (range 8 14)))))
  (is (= [0 0 4 5 0 0]
         (-> (sut/tb-var-additive-deltas)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/measure 11 4)
             (sut/get-measures (reverse (range 8 14)))))))

(deftest tb-var-latest-test
  (is (= :foobar
         (-> (sut/tb-var-latest-deltas :foobar)
             (sut/measure 12 :a)
             (sut/get-measure 10))))
  (is (= [:foobar 3 3 3]
         (as-> (sut/tb-var-latest-deltas :foobar) tb-var
           (sut/measure tb-var 10 3)
           (mapv #(sut/get-measure tb-var %) [9 10 11 20]))))
  (is (= [:foobar 2 2 2]
         (as-> (sut/tb-var-latest-deltas :foobar) tb-var
           (sut/measure tb-var 10 3)
           (sut/measure tb-var 10 2)
           (mapv #(sut/get-measure tb-var %) [9 10 11 20]))))
  (is (= 2
         (-> (sut/tb-var-latest-deltas :foobar)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/get-measure 10)))))

(deftest tb-var-test
  (is (= [:foobar :foobar 2 4 4 4]
         (-> (sut/tb-var-latest-deltas :foobar)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/measure 11 4)
             (sut/get-measures (range 8 14)))))
  (is (= 3
         (-> (sut/tb-var-additive-deltas 3)
             (sut/measure 10 3)
             (sut/measure 10 2)
             (sut/measure 11 4)
             sut/default))))

(deftest aggregator-assembly-test
  (is (= 4
         (-> [#::sut{:start-bucket 0
                     :step 10}]
             sut/aggregator
             (sut/to-bucket-aggregate 40))))
  (is (nil? (sut/validate-aggregates [#::sut{:start-bucket 0
                                             :step 10}])))
  (is some? (sut/validate-aggregates [#::sut{:step 10}]))
  (is (= [nil nil 0 0 0 0 0 1 1 1 1 1 nil nil]
         (let [agg (-> [#::sut{:start-bucket 10
                               :end-bucket 20
                               :step 5}]
                       sut/aggregator)]
           (mapv #(sut/to-bucket-aggregate agg %) (range 8 22)))))
  (is (= [0 1]
         (-> [#::sut{:start-bucket 10
                     :end-bucket 20
                     :step 5}]
             sut/aggregator
             (sut/to-bucket-aggregates (range 8 22)))))
  (is (= [nil 7 7 7 0 0]
         (let [tb-var-agg1 (-> [#::sut{:start-bucket 10
                                       :end-bucket 20
                                       :step 5}]
                               sut/aggregator
                               (sut/tb-var-aggregated (sut/tb-var-additive-deltas))
                               (sut/measure 10 3)
                               (sut/measure 14 4))]
           (mapv #(sut/get-measure tb-var-agg1 %) [9 10 11 14 15 16])))))
