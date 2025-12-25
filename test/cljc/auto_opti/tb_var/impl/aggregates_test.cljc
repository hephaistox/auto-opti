(ns auto-opti.tb-var.impl.aggregates-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti                        :as-alias opti]
   [auto-opti.tb-var.impl.aggregates :as sut]))

(deftest validate-test
  (is (every? nil?
              (mapv sut/validate
                    [nil
                     []
                     [#::opti{:start-bucket 10}]
                     [#::opti{:start-bucket 10} #::opti{:start-bucket 15}]
                     [#::opti{:start-bucket 10} #::opti{:start-bucket 15}]]))
      "Simple forms of valid `aggregates`")
  (is (every? some?
              (mapv sut/validate
                    [10
                     [#::opti{:start-bucket 10
                              :non-expected-field 10}]
                     [#::opti{:start-bucket ""}]]))
      "Invalid `aggregates`"))

(deftest default-start-bucket-of-first-aggregate-test
  (is (= [#::opti{:start-bucket 0}] (sut/default-start-bucket-of-first-aggregate [{}]))
      "Non set `start-bucket` is defaulted to `start-bucket`")
  (is (= [#::opti{:start-bucket 1}]
         (sut/default-start-bucket-of-first-aggregate [#::opti{:start-bucket 1}]))
      "If set, the `start-bucket` is not modified.")
  (is (= [#::opti{:start-bucket 1} #::opti{}]
         (sut/default-start-bucket-of-first-aggregate [#::opti{:start-bucket 1} #::opti{}]))
      "Next ones are not modified."))

(deftest make-aggregator-test
  (is (= [#::opti{:start-bucket 2
                  :end-bucket 10
                  :step 1
                  :start-bucket-aggregate 0
                  :end-bucket-aggregate 8}]
         (sut/make-aggregator [#::opti{:start-bucket 2
                                       :end-bucket 10}]))
      "An aggregate with one simple ended aggregate.")
  (is (= [#::opti{:start-bucket 2
                  :step 1
                  :start-bucket-aggregate 0}]
         (sut/make-aggregator [#::opti{:start-bucket 2}]))
      "An aggregate with one simple endless aggregate.")
  (is (= [] (sut/make-aggregator [#::opti{}])) "An aggregate with no `start-bucket` is removed.")
  (is (= [#::opti{:start-bucket 2
                  :end-bucket 5
                  :step 1
                  :start-bucket-aggregate 0
                  :end-bucket-aggregate 3}
          #::opti{:start-bucket 5
                  :end-bucket 10
                  :step 1
                  :start-bucket-aggregate 3
                  :end-bucket-aggregate 8}]
         (sut/make-aggregator [#::opti{:start-bucket 2}
                               #::opti{:start-bucket 5
                                       :end-bucket 10}]))
      "First aggregate automatically ends with the second `start-bucket`.")
  (is (= [#::opti{:start-bucket 2
                  :end-bucket 5
                  :step 1
                  :start-bucket-aggregate 0
                  :end-bucket-aggregate 3}
          #::opti{:start-bucket 5
                  :end-bucket 10
                  :step 1
                  :start-bucket-aggregate 3
                  :end-bucket-aggregate 8}]
         (sut/make-aggregator [#::opti{:start-bucket 5
                                       :end-bucket 10}
                               #::opti{:start-bucket 2}]))
      "`aggregator` is sorting the `aggregate`")
  (is (= [#::opti{:start-bucket 5
                  :start-bucket-aggregate 0
                  :step 10}]
         (sut/make-aggregator [{::opti/start-bucket 5
                                ::opti/step 10}]))
      "If `step` is set, it is not modified.")
  (is (= [#::opti{:start-bucket 2
                  :start-bucket-aggregate 0
                  :end-bucket-aggregate 1
                  :step 1
                  :end-bucket 3}
          #::opti{:start-bucket 5
                  :start-bucket-aggregate 1
                  :step 10}]
         (sut/make-aggregator [{::opti/start-bucket 5
                                ::opti/step 10}
                               {::opti/start-bucket 2
                                ::opti/end-bucket 3}]))
      "An `end-bucket` is not modified.")
  (is (sut/make-aggregator [#::opti{:start-bucket 1} #::opti{}])))
