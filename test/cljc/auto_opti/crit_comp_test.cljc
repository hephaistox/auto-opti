(ns auto-opti.crit-comp-test
  (:require
   [auto-opti.crit-comp :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest direct-eval-test
  (is (neg? (sut/direct-eval {:crit-comp-name :smaller} 10 200)) "Smaller")
  (is (pos? (sut/direct-eval {:crit-comp-name :smaller} 100 20)) "Bigger")
  (is (nil? (sut/direct-eval {} 100 20)) "If not crit-comp-name is found, eval returns nil"))
