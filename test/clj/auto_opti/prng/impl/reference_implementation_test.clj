(ns auto-opti.prng.impl.reference-implementation-test
  "Tests against C reference implementation from http://xoroshiro.di.unimi.it/"
  (:require
   [auto-opti.prng.impl.xoroshiro128-jvm :as xoro]
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

