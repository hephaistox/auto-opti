(ns auto-opti.tb.additive-test
  (:require
   [auto-opti.maths       :as opt-maths]
   [auto-opti.tb.additive :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.tb.protocol :as opt-tb]))

(deftest measure-test
  (is (= {1 12}
         (-> (sut/make)
             (opt-tb/measure 1 12)
             :deltas))
      "First element")
  (is (= {1 12
          2 13}
         (-> (sut/make)
             (opt-tb/measure 1 12)
             (opt-tb/measure 2 13)
             :deltas))
      "Second element at a different date")
  (is (= {1 25}
         (-> (sut/make)
             (opt-tb/measure 1 12)
             (opt-tb/measure 1 13)
             :deltas))
      "Second element at the same date is added"))

(deftest get-measure-test
  (is (= 100
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/get-measure 10)))
      "A measured value is returned")
  (is (= 150
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/measure 20 100)
             (opt-tb/measure 10 50)
             (opt-tb/get-measure 10)))
      "Two measured value is returned")
  (is (= 0
         (-> (sut/make)
             (opt-tb/get-measure 10)))
      "If no value is given, 0 is returned"))


(deftest clamp-test
  (is (= {}
         (-> (sut/make)
             (opt-tb/clamp 10 100)
             :deltas))
      "Clamping an empty")
  (is (= {10 100}
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/clamp 10 100)
             :deltas))
      "Clamping is not removing a delta exactly matching the start date")
  (is (= {10 100
          20 200}
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/measure 20 200)
             (opt-tb/clamp 0 100)
             :deltas))
      "Clamping is not removing deltas between `start` and `end`")
  (is (= {}
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/measure 20 200)
             (opt-tb/clamp 0 5)
             :deltas))
      "Clamping is removing all delta after `end`"))

(deftest mean-test
  (is (= 0.0
         (-> (sut/make)
             (opt-tb/mean 10 30)))
      "An empty tb mean is 0")
  (is (= 20.0
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/measure 20 100)
             (opt-tb/mean 10 20)))
      "Mean value is 100 + 100 over 10 dates, so a mean of 20"))

(deftest stddev-test
  (is (= 0.0
         (-> (sut/make)
             opt-tb/stddev))
      "An empty tb has a zero standard deviation")
  (is (= 0.0
         (-> (sut/make)
             (opt-tb/measure 10 100)
             (opt-tb/clamp 0 20)
             opt-tb/stddev))
      "A tb with one value has a zero standard deviation")
  (is (opt-maths/approx= 0.00001
                         5.92921205182911
                         (-> (sut/make)
                             (opt-tb/measure 0 100)
                             (opt-tb/measure 1 101)
                             (opt-tb/measure 2 105)
                             (opt-tb/measure 3 90)
                             (opt-tb/measure 4 90)
                             (opt-tb/measure 5 90)
                             (opt-tb/measure 6 90)
                             (opt-tb/measure 7 90)
                             (opt-tb/measure 8 90)
                             (opt-tb/measure 9 90)
                             (opt-tb/clamp 0 9)
                             opt-tb/stddev))
      "A tb with one value has a zero standard deviation"))

