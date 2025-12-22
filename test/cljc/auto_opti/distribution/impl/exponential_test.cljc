(ns auto-opti.distribution.impl.exponential-test
  (:require
   #?@(:clj [[clojure.test :refer [deftest is]]]
       :cljs [[cljs.test :refer [deftest is] :include-macros true]])
   [auto-opti.distribution                  :as opt-distribution-prot]
   [auto-opti.distribution.impl.exponential :as sut]
   [auto-opti.maths                         :as opt-maths]
   [auto-opti.prng.impl.xoroshiro128        :as opt-prng-xoro]))

(def uuid-stub #uuid "760c96be-38e4-451f-92ad-674b27121298")

(deftest draw-test
  (is (opt-maths/approx= 0.000001
                         (-> (opt-prng-xoro/make uuid-stub)
                             (sut/make 2.0)
                             opt-distribution-prot/draw)
                         1.689404)
      "Draw returns a double"))

(deftest median-test
  (is (opt-maths/approx= 0.00001
                         (-> (opt-prng-xoro/make uuid-stub)
                             (sut/make 2.0)
                             opt-distribution-prot/median)
                         0.34657)
      "Is the median of exponential 2 is (ln 2)/lambda"))

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
  {0 3592
   7 908
   20 64
   27 13
   1 2988
   24 30
   39 5
   4 1660
   15 165
   21 68
   31 7
   32 4
   40 1
   33 5
   13 285
   22 44
   36 3
   43 1
   29 17
   44 1
   6 1045
   28 9
   25 22
   34 5
   17 146
   3 1947
   12 330
   2 2439
   23 35
   35 5
   19 84
   11 404
   9 568
   5 1354
   14 215
   26 27
   16 164
   38 1
   30 7
   10 502
   18 107
   42 1
   37 3
   8 718
   49 1})

(deftest assembly-test
  (is (= assembly-test-res
         (let [t (-> (opt-prng-xoro/make uuid-stub)
                     (sut/make 0.2))]
           (->> (repeat 20000 t)
                (mapv #(int (opt-distribution-prot/draw %)))
                frequencies)))))

(comment
  (require '[com.hypirion.clj-xchart :as c])
  (c/view (c/category-chart {"Values" assembly-test-res}
                            {:title "Exponential"
                             :series-order (->> assembly-test-res
                                                keys
                                                sort
                                                (map str))
                             :theme :ggplot2})))
