(ns auto-opti.tb-test
  (:require
   [auto-opti.tb :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest finc-test
  (is (= {:deltas {30 1}} (into {} ((sut/incnil 30 :level) nil)))
      "Adding to a `nil` is creating the tb")
  (is (= {:deltas {2 1
                   30 2}}
         (into {} ((sut/incnil 30 :level) (sut/inc (sut/make-level) 2))))
      "The inc is adding one to the value at date 30"))
