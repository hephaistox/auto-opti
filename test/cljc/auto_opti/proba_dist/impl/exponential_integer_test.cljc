(ns auto-opti.proba-dist.impl.exponential-integer-test
  (:require
   #?@(:clj [[clojure.test :refer [deftest is]]]
       :cljs [[cljs.test :refer [deftest is] :include-macros true]])
   [auto-opti.maths                               :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128              :as opt-prng-xoro]
   [auto-opti.proba-dist                          :as opt-distribution-prot]
   [auto-opti.proba-dist.impl.exponential-integer :as sut]))

(def uuid-stub #uuid "760c96be-38e4-451f-92ad-674b27121298")

(deftest draw-test
  (is (= (-> (opt-prng-xoro/make uuid-stub)
             (sut/make 2.0)
             opt-distribution-prot/draw)
         1)
      "Draw returns an integer"))

(deftest median-test
  (is (= (-> (opt-prng-xoro/make uuid-stub)
             (sut/make 2.0)
             opt-distribution-prot/median)
         0)
      "Is the median of exponential 2 is 0"))

(deftest cumulative-test
  (is (opt-maths/approx= 0.0001
                         (-> (opt-prng-xoro/make uuid-stub)
                             (sut/make 2.0)
                             (opt-distribution-prot/cumulative 0.4))
                         0.5506)
      "Cumulative"))

(deftest minimun-test
  (is (-> (sut/make (opt-prng-xoro/make uuid-stub) 2.0)
          opt-distribution-prot/minimum
          zero?)
      "Minimum"))

(deftest maximun-test
  (is (-> (opt-prng-xoro/make uuid-stub)
          (sut/make 2.0)
          opt-distribution-prot/maximum
          opt-maths/infinite?)
      "Maximum"))

(deftest quantile-test
  (is (double? (-> (opt-prng-xoro/make uuid-stub)
                   (sut/make 2.0)
                   (opt-distribution-prot/quantile 0.3)))
      "Quantile"))

(def assembly-test-res
  {0 36185
   7 8982
   59 1
   20 672
   58 1
   27 144
   1 29708
   24 304
   39 23
   46 5
   4 16235
   15 1774
   48 2
   50 1
   21 579
   31 63
   32 58
   40 13
   33 59
   13 2743
   22 433
   36 35
   41 4
   43 12
   61 1
   29 104
   44 4
   6 10853
   28 124
   25 231
   34 40
   17 1270
   3 19621
   12 3214
   2 24414
   23 359
   47 2
   35 37
   19 873
   11 4040
   9 5986
   5 13543
   14 2139
   45 3
   26 204
   16 1490
   38 10
   30 105
   10 4865
   18 1020
   52 2
   42 12
   37 22
   8 7374
   49 2})

(deftest assembly-test
  (is (= assembly-test-res
         (let [t (-> (opt-prng-xoro/make uuid-stub)
                     (sut/make 0.2))]
           (->> (repeat 200000 t)
                (mapv opt-distribution-prot/draw)
                frequencies)))))

(comment
  (require '[com.hypirion.clj-xchart :as c])
  (c/view (c/category-chart {"Values" assembly-test-res}
                            {:title "Exponential integer"
                             :series-order (->> assembly-test-res
                                                keys
                                                sort
                                                (map str))
                             :theme :ggplot2})))
