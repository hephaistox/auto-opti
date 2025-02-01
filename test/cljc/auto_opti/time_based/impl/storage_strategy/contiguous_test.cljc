(ns auto-opti.time-based.impl.storage-strategy.contiguous-test
  (:require
   [auto-opti.time-based.impl.storage-strategy            :as opt-tb-ss]
   [auto-opti.time-based.impl.storage-strategy.contiguous :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest continuous-accept-k-test
  (testing "Position in the vector are accepted without any modification."
    (is (= 3
           (count (sut/continuous-accept-k [1 2 3] 1 3))
           (count (sut/continuous-accept-k [1 2 3] 0 3))
           (count (sut/continuous-accept-k [1 2 3] 2 3))
           (count (sut/continuous-accept-k [1 2 3] -2 3)))))
  (testing "Extending the vector include the position + chunk."
    (is (= 8 (count (sut/continuous-accept-k [1 2] 4 3))))))

(deftest assoc-date-test
  (testing "Contiguous is updated."
    (is (= [nil :v nil] (:contiguous (opt-tb-ss/assoc-date (sut/make 3) 1 :v)))))
  (testing "An element outside the bounds extends the contiguous."
    (is (= [nil nil nil :v nil nil] (:contiguous (opt-tb-ss/assoc-date (sut/make 3) 3 :v)))))
  (testing "A non existing element turned into a non nil value."
    (is (= 1 (opt-tb-ss/nb-set (opt-tb-ss/assoc-date (sut/make 3) 3 :v)))))
  (testing "An already existing element is not changing the number of elements."
    (is (= 1
           (opt-tb-ss/nb-set (-> (sut/make 3)
                                 (opt-tb-ss/assoc-date 3 :v)
                                 (opt-tb-ss/assoc-date 3 :v)))))))

(deftest get-after-test
  (testing "A non existing element returns nil."
    (is (nil? (-> (sut/make 3)
                  (opt-tb-ss/get-after 2)))))
  (testing "Get an existing element."
    (is (= :foo
           (-> (sut/make 3)
               (opt-tb-ss/assoc-date 2 :foo)
               (opt-tb-ss/get-after 2)))))
  (testing "The element does not exist, but one other is set afterwhile."
    (is (= :foo
           (-> (sut/make 10)
               (opt-tb-ss/assoc-date 4 :foo)
               (opt-tb-ss/get-after 2))))))

(deftest get-before-test
  (testing "A non existing element returns nil."
    (is (nil? (-> (sut/make 3)
                  (opt-tb-ss/get-before 2)))))
  (testing "Get an existing element."
    (is (= 3
           (-> (sut/make 3)
               (opt-tb-ss/assoc-date 2 3)
               (opt-tb-ss/get-before 2)))))
  (testing "The element does not exist, but one other is set beforewhile."
    (is (= :foo
           (-> (sut/make 10)
               (opt-tb-ss/assoc-date 4 :foo)
               (opt-tb-ss/get-before 6))))))

(deftest get-measures-test
  (testing "An empty range is returning nils."
    (is (= [nil nil nil nil]
           (-> (sut/make 10)
               (opt-tb-ss/get-measures (range 3 7))))))
  (testing "A range is returned."
    (is (= [nil 1 3 2]
           (-> (sut/make 10)
               (opt-tb-ss/assoc-date 4 1)
               (opt-tb-ss/assoc-date 5 3)
               (opt-tb-ss/assoc-date 6 2)
               (opt-tb-ss/get-measures (range 3 7)))))))

(deftest range-dates-test
  (is (= [2 5]
         (-> (sut/make 10)
             (opt-tb-ss/assoc-date 2 10)
             (opt-tb-ss/assoc-date 5 7)
             opt-tb-ss/range-dates)))
  (testing "Only one value returns a zero length interval."
    (is (= [5 5]
           (-> (sut/make 10)
               (opt-tb-ss/assoc-date 5 7)
               opt-tb-ss/range-dates))))
  (testing "Only one value returns a zero length interval."
    (is (= [nil nil]
           (-> (sut/make 10)
               opt-tb-ss/range-dates)))))

(deftest update-date-test
  (is (= 2
         (-> (sut/make 10)
             (opt-tb-ss/update-date 4 (fnil inc 0) [])
             (opt-tb-ss/update-date 4 (fnil inc 0) [])
             (opt-tb-ss/get-exact 4)))))

(deftest occupation-rate-test
  (is (= 0
         (-> (sut/make 10)
             opt-tb-ss/occupation-rate)))
  (is (= #?(:clj 1/10
            :cljs 0.1)
         (-> (sut/make 10)
             (opt-tb-ss/assoc-date 2 10)
             opt-tb-ss/occupation-rate)))
  (is (= 1
         (-> (sut/make 2)
             (opt-tb-ss/assoc-date 0 10)
             (opt-tb-ss/assoc-date 1 10)
             opt-tb-ss/occupation-rate))))
