(ns auto-opti.time-based.impl.aggregator-item-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.time-based                      :as opt-tb]
   [auto-opti.time-based.impl.aggregator-item :as sut]))

(deftest bucket-aggregate-test
  (is (every? nil?
              (mapv (partial sut/bucket-aggregate
                             #::opt-tb{:start-bucket 10
                                       :step 2
                                       :end-bucket 12
                                       :start-bucket-aggregate 3})
                    [-10 8 12 100]))
      "Out of bounds values returns nil.")
  (is (= [3 3]
         (mapv (partial sut/bucket-aggregate
                        #::opt-tb{:start-bucket 10
                                  :step 2
                                  :end-bucket 12
                                  :start-bucket-aggregate 3})
               [10 11])))
  (is (= [3 4 4 5]
         (mapv (partial sut/bucket-aggregate
                        #::opt-tb{:start-bucket 10
                                  :step 2
                                  :end-bucket 15
                                  :start-bucket-aggregate 3})
               [11 12 13 14]))
      "The second `bucket-aggregate` in the `aggregate-item` is returned.")
  (is (= [3 48 4998]
         (mapv (partial sut/bucket-aggregate
                        #::opt-tb{:start-bucket 10
                                  :step 2
                                  :start-bucket-aggregate 3})
               [10 100 10000]))
      "Aggregate with no end-date is found as infinite."))

(deftest bucket-range--test
  (is (every? nil?
              (mapv (partial sut/bucket-range
                             #::opt-tb{:start-bucket 10
                                       :step 2
                                       :end-bucket 20
                                       :start-bucket-aggregate 3
                                       :end-bucket-aggregate 8})
                    [-10 2 8 100]))
      "Out of bound values returns nil.")
  (is (= [[10 12] [12 14] [14 16]]
         (mapv (partial sut/bucket-range
                        #::opt-tb{:start-bucket 10
                                  :step 2
                                  :end-bucket 20
                                  :start-bucket-aggregate 3
                                  :end-bucket-aggregate 8})
               [3 4 5]))))

(deftest calculate-end-bucket-aggregate-test
  (is (let [v #::opt-tb{:start-bucket 10
                        :step 2
                        :start-bucket-aggregate 3}]
        (= v (sut/calculate-end-bucket-aggregate v)))
      "`aggregator` with no `end-bucket` are removed.")
  (is (= 4
         (-> #::opt-tb{:start-bucket 10
                       :step 2
                       :end-bucket 12
                       :start-bucket-aggregate 3}
             sut/calculate-end-bucket-aggregate
             ::opt-tb/end-bucket-aggregate))
      "Adds the `end-bucket-aggregate` to the `aggregator`"))

(deftest build-test
  (is (= #::opt-tb{:start-bucket-aggregate 10} (sut/build 10 {}))
      "Aggregator-item with no `end-bucket` is returning nothing.")
  (is (= #::opt-tb{:start-bucket 3
                   :end-bucket 10
                   :step 1
                   :start-bucket-aggregate 10
                   :end-bucket-aggregate 17}
         (sut/build 10
                    #::opt-tb{:start-bucket 3
                              :end-bucket 10
                              :step 1}))
      "Aggregator-item with `end-bucket` is updating `end-bucket-aggregate`."))

(deftest bucket-to-bucket-aggregate-test
  (is (= [30 30 31 31 32 32]
         (sut/bucket-to-bucket-aggregate #::opt-tb{:start-bucket 10
                                                   :end-bucket 16
                                                   :step 2
                                                   :start-bucket-aggregate 30
                                                   :end-bucket-aggregate 33})))
  (is (empty? (sut/bucket-to-bucket-aggregate #::opt-tb{:start-bucket 10
                                                        :end-bucket 10
                                                        :step 2
                                                        :start-bucket-aggregate 30
                                                        :end-bucket-aggregate 30}))
      "Empty aggregate is ok.")
  (is
   (= [30 30]
      (sut/bucket-to-bucket-aggregate #::opt-tb{:start-bucket 10
                                                :end-bucket 12
                                                :start-bucket-aggregate 30
                                                :end-bucket-aggregate 31
                                                :step 2}))
   "A translation ending exactly at the last element of the step - just before the creation of the next bucket-aggregate.")
  (is (= [30 30 31]
         (sut/bucket-to-bucket-aggregate #::opt-tb{:start-bucket 10
                                                   :end-bucket 13
                                                   :step 2
                                                   :start-bucket-aggregate 30
                                                   :end-bucket-aggregate 32}))
      "An aggregate ending before the end of the aggregate."))

(deftest concerns-bucket-test
  (is
   (= [false false true true false false]
      (mapv #(sut/concerns-bucket? %
                                   #::opt-tb{:start-bucket 3
                                             :end-bucket 30})
            [0 2 3 29 30 100]))
   "An `aggregator` with `start-bucket` and `end-bucket` is concerned with `[start-bucket;end-bucket[` and only that.")
  (is
   (sut/concerns-bucket? 30 #::opt-tb{:start-bucket 3})
   "An `aggregator` with `start-bucket` and no `end-bucket` is concerned with everything after `start-bucket`."))

(deftest concerns-bucket-aggregate-test
  (is
   (= [false false true true false false false]
      (mapv #(sut/concerns-bucket-aggregate? %
                                             #::opt-tb{:start-bucket-aggregate 3
                                                       :end-bucket-aggregate 30})
            [0 2 3 29 30 31 1000]))
   "An `aggregator` with `start-bucket-aggregate` and `end-bucket-aggregate` is concerned with `[start-bucket-aggregate;end-bucket-aggregate[` and only that.")
  (is
   (sut/concerns-bucket-aggregate? 30 #::opt-tb{:start-bucket-aggregate 3})
   "An `aggregator` with `start-bucket-aggregate` and no `end-bucket-aggregate` is concerned with everything after `start-bucket-aggregate`."))
