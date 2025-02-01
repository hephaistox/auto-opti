(ns auto-opti.maths.weighted-sum-test
  (:require
   [auto-opti.maths.weighted-sum :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest weighted-sum-test
  (is (= 320
         (sut/weighted-sum {:a 10
                            :b 100}
                           {:a 12
                            :b 2}))
      "Weighted sum calculated")
  (is (= 200
         (sut/weighted-sum {:a 10
                            :b 100}
                           {:b 2}))
      "Missing criteria are skipped"))
