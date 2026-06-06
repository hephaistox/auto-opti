(ns auto-opti.prng.impl.reference-implementation-test
  "Tests against C reference implementation from http://xoroshiro.di.unimi.it/"
  (:require
   [auto-opti.prng.impl.xoroshiro128-jvm :as xoro]
   [auto-opti.prng.impl.xoroshiro256-jvm :as xoro256]
   [clojure.test                         :refer [deftest is testing]]))

;; Reference values generated from C implementation with state [1, 2]
(def reference-values-state-1-2
  "First 20 values from xoroshiro128+ (2018 version: a=24, b=16, c=37) 
   with initial state s[0]=1, s[1]=2
  
  Generated with run in test/c/run-xoro"
  ["3"
   "412333834243"
   "2360170716294286339"
   "9295852285959843169"
   "2797080929874688578"
   "6019711933173041966"
   "3076529664176959358"
   "3521761819100106140"
   "7493067640054542992"
   "920801338098114767"
   "7981395621054412125"
   "7824779138144814671"
   "15751912171670465156"
   "13002195027962367578"
   "1252975949485787994"
   "10593145921556528063"
   "10251274063555716327"
   "8001051857350374592"
   "13050593483651723543"
   "15768724955744760659"])

(deftest test-against-c-reference-state-1-2
  (testing "Compare against C reference implementation with state [1, 2]"
    (let [state (long-array [1 2])
          actual (vec (repeatedly 20 #(xoro/xoroshiro-next! state)))
          ;; Convert to unsigned strings for comparison (since C prints unsigned)
          actual-strs (vec (map #(Long/toUnsignedString %) actual))]
      (is (= reference-values-state-1-2 actual-strs)
          "Should match C reference implementation exactly (as unsigned strings)"))))

;; Reference values generated from C implementation with state [1, 2, 3, 4]
(def reference-values-256-state-1-2-3-4
  "First 20 values from xoshiro256+ (parameters a=17, b=45) with initial state
   s[0]=1, s[1]=2, s[2]=3, s[3]=4.

  Generated with `test/c/run256` (test/c/xoro256_test.cpp)."
  ["5"
   "211106232532999"
   "211106635186183"
   "9223759065350669058"
   "9250833439874351877"
   "13862484359527728515"
   "2346507365006083650"
   "1168864526675804870"
   "34095955243042024"
   "3466914240207415127"
   "11255383954288805324"
   "12715405123800208740"
   "16628236566668953827"
   "9317817742930101600"
   "11450165966258622621"
   "3121368479735453605"
   "17859575816095342282"
   "1537078577578533333"
   "17413408203025879122"
   "13777020826476661019"])

(deftest test-against-c-reference-256-state-1-2-3-4
  (testing "Compare xoshiro256+ against C reference implementation with state [1, 2, 3, 4]"
    (let [state (long-array [1 2 3 4])
          actual (vec (repeatedly 20 #(xoro256/xoroshiro-next! state)))
          actual-strs (vec (map #(Long/toUnsignedString %) actual))]
      (is (= reference-values-256-state-1-2-3-4 actual-strs)
          "Should match C reference implementation exactly (as unsigned strings)"))))

