(ns auto-opti.prng.impl.xoroshiro128-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-opti.prng.impl.tests        :as opt-prng-tests]
   [auto-opti.prng.impl.xoroshiro128 :as sut]
   [auto-opti.prng.stateful          :as opt-prng-stateful]
   [auto-opti.prng.stateless         :as opt-prng-stateless]
   [auto-opti.prng.stateless-test    :as opt-prng-stateless-test]
   [cljc-long.core]
   [xoroshiro128.core                :as xoro]))

(deftest xoro-test
  (is (opt-prng-stateless-test/test-all (sut/make-stateless))
      "Stateless tests for xoro are passing"))

(deftest test-uniformity-test
  (is (nil? (-> (sut/make #uuid "e8971453-69c4-499f-a010-53e1e145ee7f")
                (opt-prng-stateful/as-ints 10000 0 10)
                (opt-prng-tests/distribution-uniformity 3)))
      "Uniformity of test is below 3"))

(deftest xoro-lib-test
  (is (=
       ["-8599790385339156450"
        "-2397555347192430356"
        "6070147179690023442"
        "-5761889779339854540"
        "4712530076458880124"
        "-6281817213406599400"
        "-4444086269195772773"
        "7597340901018260824"
        "1097062382358739162"
        "-8437855807897079762"]
       (let [uuid #uuid "e8971453-69c4-499f-a010-53e1e145ee7f"
             seed (xoro/uuid->seed128 uuid)]
         (loop [prng (xoro/xoroshiro128+ seed)
                res []
                n 10]
           (if (pos? n) (recur (xoro/next prng) (conj res (str (xoro/value prng))) (dec n)) res))))
      "Is xoroshiro library finding the same result ?"))

(deftest make-stateless-test
  (is (= [43550 69644 23442 45460 80124 600 27227 60824 39162 20238]
         (loop [prng (sut/make-stateless #uuid "e8971453-69c4-499f-a010-53e1e145ee7f")
                res []
                n 10]
           (if (pos? n)
             (recur (opt-prng-stateless/next prng)
                    (conj res (opt-prng-stateless/peek-int prng 0 100000))
                    (dec n))
             res)))
      "Is the stateless implementation of the xoro prng is stable in terms of results?")
  (is (= [10 14 12 10 14 10 17 14 12 18]
         (loop [prng (sut/make-stateless #uuid "e8971453-69c4-499f-a010-53e1e145ee7f")
                res []
                n 10]
           (if (pos? n)
             (recur (opt-prng-stateless/next prng)
                    (conj res (opt-prng-stateless/peek-int prng 10 20))
                    (dec n))
             res)))
      "Is rnd-int returning the same value?"))
