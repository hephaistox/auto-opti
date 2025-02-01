(ns auto-opti.prng-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.prng :as sut]))

(deftest prng-test (is (nil? (sut/prng {})) "prng is a mandatory parameter"))
