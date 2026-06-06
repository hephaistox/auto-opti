(ns auto-opti.eval.montecarlo-test
  (:require
   [auto-opti.eval.montecarlo :as sut]
   [auto-opti.maths           :as opt-maths]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(defn close-to-pi "Is `x` close to `π`?" [x] (<= (opt-maths/abs (- x opt-maths/PI)) 0.01))

(deftest eval-test
  (is (-> (sut/eval {}
                    nil
                    {:radius 100
                     :iterations 100000}
                    {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"})
          close-to-pi)
      "Evaluation finds close to 3")
  (is (= {:nb-in 77
          :it 100
          :intermediate-criteria 3.08
          :iterations 100}
         (let [x (atom nil)]
           (sut/eval {}
                     (fn [iterator-params] (reset! x iterator-params))
                     {:radius 100
                      :iterations 100}
                     {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fd"})
           @x))
      "iterator call")
  (is (nil? (sut/eval nil nil {:radius ""} {})) "radius should be a string")
  (is (nil? (sut/eval nil nil {:iterations ""} {})) "iterations should be a string")
  (is (nil? (sut/eval nil nil {} {:seed nil})) "seed can be skipped"))

(deftest valid-pars-test
  (is (= {:model {:error {:radius ["should be an integer"]
                          :iterations ["should be an integer"]}
                  :schema [:map [:radius :int] [:iterations :int]]
                  :data {:radius "r"
                         :iterations "i"}}}
         (sut/valid-pars {}
                         nil
                         {:radius "r"
                          :iterations "i"}))
      "Errors detected.")
  (is (nil? (sut/valid-pars {}
                            nil
                            {:radius 10
                             :iterations 1}))
      "Returns `nil` if valid."))
