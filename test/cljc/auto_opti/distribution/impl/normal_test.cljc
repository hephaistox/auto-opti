(ns auto-opti.distribution.impl.normal-test
  (:require
   [auto-opti.distribution             :as opt-dstb]
   [auto-opti.distribution.impl.normal :as sut]
   [auto-opti.maths                    :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128   :as opt-prng-xoro]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(def uuid-stub #uuid "760c96be-38e4-451f-92ad-674b27121298")

(deftest draw-test
  (is (= 9.673475985627203
         (-> (opt-prng-xoro/make uuid-stub)
             (sut/make 2.0 3.0)
             opt-dstb/draw))
      "Draw returns an integer"))

(deftest median-test
  (is (= 2.0
         (-> (opt-prng-xoro/make uuid-stub)
             (sut/make 2.0 3.0)
             opt-dstb/median))
      "Is the median of exponential 2 is 0"))

(deftest cumulative-test
  (is (opt-maths/approx= 0.0001
                         (-> (opt-prng-xoro/make uuid-stub)
                             (sut/make 2.0 3.0)
                             (opt-dstb/cumulative 0.4))
                         0.2969)
      "Cumulative"))

(deftest minimun-test
  (is (-> (opt-prng-xoro/make uuid-stub)
          (sut/make 2.0 3.0)
          opt-dstb/minimum
          opt-maths/infinite?)
      "Minimum"))

(deftest maximun-test
  (is (-> (opt-prng-xoro/make uuid-stub)
          (sut/make 2.0 3.0)
          opt-dstb/maximum
          opt-maths/infinite?)
      "Maximum"))

(deftest quantile-test
  (is (double? (-> (opt-prng-xoro/make uuid-stub)
                   (sut/make 2.0 3.0)
                   (opt-dstb/quantile 0.3)))
      "Quantile"))

(def assembly-test-res
  {205 104
   206 27
   204 334
   197 1811
   192 2
   193 19
   191 1
   195 356
   196 891
   198 2964
   201 2993
   202 1840
   200 3800
   203 925
   207 4
   194 106
   199 3823})

(deftest assembly-test
  (is (= assembly-test-res
         (let [t (-> (opt-prng-xoro/make uuid-stub)
                     (sut/make 200 2.0))]
           (->> (repeat 20000 t)
                (mapv #(int (opt-dstb/draw %)))
                frequencies)))))

(comment
  (require '[com.hypirion.clj-xchart :as c])
  (c/view (c/category-chart {"Values" assembly-test-res}
                            {:title "Normal"
                             :series-order (->> assembly-test-res
                                                keys
                                                sort
                                                (map str))
                             :theme :ggplot2})))

;; ********************************************************************************
;; Normal Integer
;; ********************************************************************************

(deftest draw-integer-test
  (is (= 9
         (-> (opt-prng-xoro/make uuid-stub)
             (sut/make-integer 2.0 3.0)
             opt-dstb/draw))
      "Draw returns an integer"))

(deftest median-integer-test
  (is (= 2.0
         (-> (opt-prng-xoro/make uuid-stub)
             (sut/make-integer 2.0 3.0)
             opt-dstb/median))
      "Is the median of exponential 2 is 0"))

(deftest cumulative-integer-test
  (is (opt-maths/approx= 0.0001
                         (-> (opt-prng-xoro/make uuid-stub)
                             (sut/make-integer 2.0 3.0)
                             (opt-dstb/cumulative 3))
                         0.6305)
      "Cumulative"))

(deftest minimun-integer-test
  (is (-> (opt-prng-xoro/make uuid-stub)
          (sut/make-integer 2.0 3.0)
          opt-dstb/minimum
          opt-maths/infinite?)
      "Minimum"))

(deftest maximun-integer-test
  (is (-> (opt-prng-xoro/make uuid-stub)
          (sut/make-integer 2.0 3.0)
          opt-dstb/maximum
          opt-maths/infinite?)
      "Maximum"))

(deftest quantile-integer-test
  (is (integer? (-> (opt-prng-xoro/make uuid-stub)
                    (sut/make-integer 2.0 3.0)
                    (opt-dstb/quantile 0.4)))
      "Quantile"))

(def assembly-integer-test-res
  {205 104
   206 27
   204 334
   197 1811
   192 2
   193 19
   191 1
   195 356
   196 891
   198 2964
   201 2993
   202 1840
   200 3800
   203 925
   207 4
   194 106
   199 3823})

(deftest assembly-integer-test
  (is (= assembly-integer-test-res
         (let [t (-> (opt-prng-xoro/make uuid-stub)
                     (sut/make-integer 200 2.0))]
           (->> (repeat 20000 t)
                (mapv #(int (opt-dstb/draw %)))
                frequencies)))))

(comment
  (require '[com.hypirion.clj-xchart :as c2])
  (c2/view (c/category-chart {"Values" assembly-integer-test-res}
                             {:title "Normal"
                              :series-order (->> assembly-integer-test-res
                                                 keys
                                                 sort
                                                 (map str))
                              :theme :ggplot2})))
