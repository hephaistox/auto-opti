(ns auto-opti.maths-test
  (:require
   [auto-opti.maths :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(defn- test-fn
  "Test `fut` (function under test) against `val`.

  `comparison` is a pair of `v` and `expected-value`.
  the returned valued is:
  * `true` if `(equal? r (fut expected-value=))`.
  * a map describing the mismatch otherwise."
  [fut equal? [v comparison-value :as _comparison]]
  (let [eq? (equal? comparison-value (fut v))]
    (if eq?
      true
      {:v v
       :r comparison-value
       :eq? eq?
       :fut-v (fut v)})))

(defn- test-fn-vals
  "Do the `test-fn` comparison for each val in `vals`."
  [fut equal? vals]
  (->> vals
       (map (partial test-fn fut equal?))
       (remove true?)))

(def ^:private int-compare =)

(def ^:private double-compare (partial sut/approx= 0.00001))

(deftest signum-test
  (is (pos? (sut/signum 10.3)))
  (is (zero? (sut/signum 0.0)))
  (is (neg? (sut/signum -10.0))))

(deftest abs-test
  (testing "Happy path"
    (is (empty? (test-fn-vals sut/abs = [[-1 1] [1 1] [0 0] [-1000 1000]])))
    (is (empty? (test-fn-vals sut/abs double-compare [[10.5 10.5] [-1.5 1.5]])))
    (is (empty? (test-fn-vals sut/abs-int int-compare [[5 5] [-2 2]])))
    (is (empty? (test-fn-vals sut/abs-double double-compare [[0.5 0.5] [-0.5 0.5]])))
    (is (not (neg? (sut/abs-double 0.0))))))

(deftest sqrt-test
  (testing "Happy path"
    (is (empty? (test-fn-vals sut/sqrt double-compare [[4 2.0] [0 0.0] [16 4.0] [1.96 1.4]])))
    (is (empty? (test-fn-vals sut/sqrt double-compare [[0 0] [4 2]])))))

(deftest square-test
  (is (empty? (test-fn-vals sut/square double-compare [[0 0.0] [4 16.0]])))
  (is (empty? (test-fn-vals sut/square-int int-compare [[0 0] [4 16] [-2 4]])))
  (is (empty? (test-fn-vals sut/square-double double-compare [[0 0] [4 16] [-2.0 4]]))))

(deftest pow-test (is (double-compare (sut/pow 3.0 3.0) 27.0)))

(deftest root-test (is (double-compare (sut/root 27.0 3.0) 3.0)))

(deftest log-test (is (double-compare (sut/log (sut/exp 3.0)) 3.0)))

(deftest log1p-test (is (double-compare (sut/log1p (- (sut/exp 4.0) 1.0)) 4.0)))

(deftest exp-test (is (double-compare (sut/exp 1) 2.718281828459045)))

(deftest cos-test
  (is (double-compare (sut/cos sut/PI) -1.0))
  (is (double-compare (sut/cos 0) 1.0))
  (is (double-compare (sut/cos sut/HALF_PI) 0.0))
  (is (double-compare (sut/cos (- sut/HALF_PI)) 0.0)))

(deftest sin-test
  (is (double-compare (sut/sin sut/PI) 0.0))
  (is (double-compare (sut/sin 0) 0.0))
  (is (double-compare (sut/sin sut/HALF_PI) 1.0))
  (is (double-compare (sut/sin (- sut/HALF_PI)) -1.0)))

(deftest tan-test
  (is (double-compare (sut/tan 0) 0.0))
  (is (double-compare (sut/tan sut/PI) 0.0))
  (is (>= (sut/tan sut/HALF_PI) 100000.0))
  (is (<= (sut/tan (- sut/HALF_PI)) -100000.0)))

(deftest atan-test
  (is (double-compare (sut/atan 0) 0.0))
  (is (double-compare (sut/atan sut/infinity) sut/HALF_PI))
  (is (double-compare (sut/atan sut/negative-infinity) (- sut/HALF_PI))))

(deftest clamp-test
  (is (double-compare (sut/clamp 1 13.0 15) 13.0))
  (is (double-compare (sut/clamp 1 13.0 13) 13.0))
  (is (double-compare (sut/clamp 1 13.0 11) 11.0))
  (is (double-compare (sut/clamp 1 13.0 1) 1.0))
  (is (double-compare (sut/clamp 1 13.0 0) 1.0)))

(deftest clamp-double-test
  (is (double-compare (sut/clamp-double 1.0 13.0 15) 13.0))
  (is (double-compare (sut/clamp-double 1.0 13.0 13) 13.0))
  (is (double-compare (sut/clamp-double 1.0 13.0 11) 11.0))
  (is (double-compare (sut/clamp-double 1.0 13.0 1) 1.0))
  (is (double-compare (sut/clamp-double 1.0 13.0 0.0) 1.0)))

(deftest clamp-integer-test
  (is (int-compare (sut/clamp-integer 1 13 15) 13))
  (is (int-compare (sut/clamp-integer 1 13 13) 13))
  (is (int-compare (sut/clamp-integer 1 13 11) 11))
  (is (int-compare (sut/clamp-integer 1 13 1) 1))
  (is (int-compare (sut/clamp-integer 1 13 0) 1)))

(deftest ceil-test
  (is (double-compare (sut/ceil 3.0) 3.0))
  (is (double-compare (sut/ceil -3.3) -3.0)))

(deftest floor-test
  (is (double-compare (sut/floor -3.0) -3.0))
  (is (double-compare (sut/floor 4.3) 4.0)))

(deftest interval-affine-test
  (testing "Identity between 0 an 10"
    (is (empty? (test-fn-vals (partial sut/interval-affine-fn 1 0 0 10)
                              double-compare
                              [[-1 nil] [10 nil] [100 nil] [0 0] [1 1]]))))
  (testing "Infinite interval"
    (is (empty?
         (test-fn-vals (partial sut/interval-affine-fn 1 0 nil nil) double-compare [[-1 -1]])))
    (is (empty? (test-fn-vals (partial sut/interval-affine-fn 1 0 10 nil)
                              double-compare
                              [[-1 nil] [9 nil] [10 10] [10000 10000]])))
    (is (empty? (test-fn-vals (partial sut/interval-affine-fn 1 0 nil 10)
                              double-compare
                              [[-1 -1] [9 9] [10 nil] [10000 nil]]))))
  (testing "Move range"
    (is (empty? (test-fn-vals (partial sut/interval-affine-fn 2 0 100 110)
                              double-compare
                              [[-1 nil] [10 nil] [99 nil] [100 200] [105 210] [112 nil]]))))
  (testing "Function not crossing origin"
    (is (empty? (test-fn-vals (partial sut/interval-affine-fn 2 10 0 10)
                              double-compare
                              [[0 10] [1 12]])))))

(deftest proportion-test
  (is (= 1.0 (sut/proportion even? [10 2])))
  (is (= 0.5 (sut/proportion even? [10 3])))
  (is (= 0.9 (sut/proportion even? [10 2 10 2 20 1 50 22 54 90])))
  (testing "Proportion of a nil set is 0"
    (is (nil? (sut/proportion even? nil)))
    (is (nil? (sut/proportion even? [])))))

(deftest polynomial-value-test
  (testing "When polynomial is constant." (is (= 1 (sut/polynomial-value 1 [1]))))
  (testing "Or even an empty polynom."
    (is (nil? (sut/polynomial-value 2 [])))
    (is (nil? (sut/polynomial-value 2 nil))))
  (is (= 6 (sut/polynomial-value 1 [3 2 1])))
  (is (= 17 (sut/polynomial-value 2 [3 2 1]))))
