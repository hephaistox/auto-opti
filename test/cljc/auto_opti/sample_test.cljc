(ns auto-opti.sample-test
  (:require
   [auto-opti.maths  :as opt-maths]
   [auto-opti.sample :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest sampling-rounded-test
  (is (= {0 6
          10 4
          20 2
          30 3
          40 9
          50 4
          60 7
          70 4
          80 4
          90 2}
         (-> (concat
              [32 73 30 41 58 39 55 24 63 81 62 68 40 70 51 16]
              [6 10 16 45 57 69 78 4 64 83 63 70 80 7 69 13 0 42 7 99 84 6 48 46 94 44 41 21 43])
             (sut/freq 10)))))

(deftest average-test
  (testing "Empty set returns nil" (is (nil? (sut/average nil))) (is (nil? (sut/average []))))
  (testing "Simple average of a collection is found"
    (is (= 5 (sut/average [2 4 4 4 5 5 7 9])))
    (is (= 14 (sut/average [14])))
    (is (= 8 (sut/average [7 9])))
    (is (= 8.0 (sut/average [7.5 8.5]))))
  (testing "Simple Gauss sum is calculated"
    (let [n 1000000] (is (= (/ (inc n) 2) (sut/average (take n (iterate inc 1))))))))

(comment
  ;; To measure variance computation time
  (require '[criterium.core :as crit])
  (crit/bench (sut/variance (take 1000 (iterate inc 1))))
  ;
)

(deftest variance-test
  (testing "Empty" (is (nil? (sut/variance nil))) (is (nil? (sut/variance []))))
  (is (zero? (sut/variance [2])) "Singleton has a zero variance")
  (is (opt-maths/approx= 0.001 4.0 (sut/variance [2 4 4 4 5 5 7 9]))))

(deftest standard-deviation-test
  (testing "Empty set has no standard deviation"
    (is (nil? (sut/standard-deviation nil)))
    (is (nil? (sut/standard-deviation []))))
  (testing "Singleton has a zero stdev" (is (zero? (sut/standard-deviation [4]))))
  (testing "Simple example" (is (= 2.0 (sut/standard-deviation [2 4 4 4 5 5 7 9])))))

(deftest median-test
  (testing "Median of an odd number of doubles is the one in the middle."
    (is (= 14 (sut/median [1 10 14 17 2000]))))
  (testing "Median of an even number of numbers is the average of the two middle numbers."
    (is (= 15 (sut/median [1 10 20 1000])))))

(deftest midrange-test
  (testing "Midrange is ok"
    (is (= 50 (sut/midrange-long [2 98 13 10]) (sut/midrange-long [2 98])))
    (is (opt-maths/approx= 0.0001 50.0 (sut/midrange-double [2 98 13 10])))
    (is (opt-maths/approx= 0.0001 50.0 (sut/midrange-double [2 98]))))
  (testing "Midrange is ok"
    (is (nil? (sut/midrange [])))
    (is (nil? (sut/midrange nil)))
    (is (= 10 (sut/midrange [10])))))

(comment
  ;; Performance measurements of benchmark function
  (crit/bench (sut/average (take 1000 (iterate inc 1))))
  (crit/report-result (crit/benchmark (sut/average (take 1000 (iterate inc 1))) {:verbose true}))
  ;
)

(deftest erf-test (is (= 1.0 (sut/erf 10))) (is (= 0.0 (sut/erf 0))))
