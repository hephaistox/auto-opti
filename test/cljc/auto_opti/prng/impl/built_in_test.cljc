(ns auto-opti.prng.impl.built-in-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti.prng.impl.built-in :as sut]
   [auto-opti.prng.impl.tests    :as opt-prng-tests]
   [auto-opti.prng.stateful      :as opt-prng-stateful]
   [auto-opti.prng.stateful-test :as opt-prng-stateful-test]))

(deftest built-in-test
  (testing "Is built-in a stateful prng?" (opt-prng-stateful-test/test-non-repeatable (sut/make))))

(deftest test-uniformity
  (is (nil? (opt-prng-tests/distribution-uniformity (opt-prng-stateful/as-ints (sut/make) 1000 0 30)
                                                    3))
      "Uniformity of test is below 3"))
