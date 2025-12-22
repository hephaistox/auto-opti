(ns auto-opti.prng-test
  (:require
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])
   [auto-opti      :as-alias opti]
   [auto-opti.prng :as sut]))

(deftest prng-test
  (testing "Simple value"
    (is (= 6
           (-> (sut/prng {})
               (sut/as-int 0 10))
           (-> (sut/prng #::sut{:registry nil
                                :seed nil
                                :prg-name nil})
               (sut/as-int 0 10)))
        "Returns a fix int with default xoroshiro's prng and default seed")
    (is (= 0.2594879491552782
           (-> (sut/prng {})
               (sut/as-double 0.0 10.0)))
        "Returns a fix int with default xoroshiro's prng and default seed"))
  (testing "Three times"
    (is (= [6 7 67]
           ((juxt #(sut/as-int % 0 10) #(sut/as-int % 6 12) #(sut/as-int % 60 72)) (sut/prng {})))
        "Returns a fix int with default xoroshiro's prng and default seed")
    (is (= [0.2594879491552782 9.485617640334087 71.86763663280476]
           ((juxt #(sut/as-double % 0 10) #(sut/as-double % 6 12) #(sut/as-double % 60 72))
            (sut/prng {})))
        "Returns a fix int with default xoroshiro's prng and default seed")))

(deftest prng-pair
  (is (= [[56 59] [89.0 74.0]]
         (let [prng* (-> {}
                         sut/prng)]
           (vector (sut/as-int-pair prng* 10 100) (sut/as-double-pair prng* 10.0 100.0))))
      "Return pairs"))

(deftest prng-set
  (is (= [[56 59 89] [44.32935272914683 47.020137246995276 99.99216829772887]]
         (let [prng* (-> {}
                         sut/prng)]
           (vector (sut/as-ints prng* 3 10 100) (sut/as-doubles prng* 3 10.0 100.0))))
      "Returns set of values"))

(deftest reuse-seed
  (is (= [56 62.28426460501133 56 62.28426460501133]
         (let [prng* (-> #::sut{:prng-name :xoroshiro128
                                :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                         sut/prng)
               prng2* (-> #::sut{:prng-name :xoroshiro128
                                 :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                          sut/prng)]
           (vector (sut/as-int prng* 10 100)
                   (sut/as-double prng* 10.0 100.0)
                   (sut/as-int prng2* 10 100)
                   (sut/as-double prng2* 10.0 100.0))))
      "With the same seed, the same results are found"))

(deftest duplicate
  (is (= [[56 62.28426460501133] [56 62.28426460501133]]
         (let [prng* (-> #::sut{:prng-name :xoroshiro128
                                :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                         sut/prng)
               tmp (vector (sut/as-int prng* 10 100) (sut/as-double prng* 10.0 100.0))
               prng2* (-> #::sut{:prng-name :xoroshiro128
                                 :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                          sut/prng)]
           (vector tmp (vector (sut/as-int prng2* 10 100) (sut/as-double prng2* 10.0 100.0)))))
      "With the same seed, the same results are found"))

(deftest uuid-seed
  (is (= [[56 62.28426460501133] [56 62.28426460501133]]
         (let [prng* (-> #::sut{:prng-name :xoroshiro128
                                :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                         sut/prng)
               tmp (vector (sut/as-int prng* 10 100) (sut/as-double prng* 10.0 100.0))
               prng2* (-> #::sut{:prng-name :xoroshiro128
                                 :seed (sut/uuid-seed prng*)}
                          sut/prng)]
           (vector tmp (vector (sut/as-int prng2* 10 100) (sut/as-double prng2* 10.0 100.0)))))
      "Using the seed comes back at the beginning of the random sequence"))

(deftest reset
  (is (= [[56 62.28426460501133] [56 62.28426460501133]]
         (let [prng* (-> #::sut{:prng-name :xoroshiro128
                                :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}
                         sut/prng)
               tmp (vector (sut/as-int prng* 10 100) (sut/as-double prng* 10.0 100.0))
               prng2* (-> #::sut{:prng-name :xoroshiro128
                                 :seed (sut/uuid-seed prng*)}
                          sut/prng)]
           (vector tmp (vector (sut/as-int prng2* 10 100) (sut/as-double prng2* 10.0 100.0)))))
      "Reset comes back to seed"))

(deftest change-prng
  (is (= 6
         (-> (sut/prng #::sut{:prng-name :xoroshiro128
                              :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"})
             (sut/as-int 0 10)))
      "Xoroshiro"))
