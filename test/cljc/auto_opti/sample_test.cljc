(ns auto-opti.sample-test
  (:require
   [auto-opti.sample :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(comment
  (repeatedly 100
              #(-> (rand)
                   (* 100)
                   int))
  ;;
)

(deftest sampling-rounded-test
  (is (= {0 6
          70 4
          20 2
          60 7
          50 4
          40 9
          90 2
          30 3
          10 4
          80 4}
         (sut/sampling-rounded
          (concat
           [32 73 30 41 58 39 55 24 63 81 62 68 40 70 51 16]
           [6 10 16 45 57 69 78 4 64 83 63 70 80 7 69 13 0 42 7 99 84 6 48 46 94 44 41 21 43])
          10))))
