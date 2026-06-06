(ns auto-opti.iterative.descent-test
  (:require
   [auto-opti.iterative.descent :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

;; `auto-opti.iterative.descent` is EXPERIMENTAL and not implemented yet. This test
;; documents and pins that contract: `stochastic` must clearly fail rather than run
;; broken half-logic. Replace it with real behavioural tests once the descent loop is
;; finished and promoted out of quarantine.
(deftest stochastic-not-implemented-test
  (testing "stochastic is experimental and throws until implemented"
    (is (thrown-with-msg? #?(:clj clojure.lang.ExceptionInfo
                             :cljs cljs.core/ExceptionInfo)
                          #"not implemented yet"
                          (sut/stochastic nil 0 nil nil nil nil nil)))))
