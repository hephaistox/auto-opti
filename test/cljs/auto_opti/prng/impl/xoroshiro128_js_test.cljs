(ns auto-opti.prng.impl.xoroshiro128-js-test
  (:require
   [auto-opti                           :as-alias opti]
   [auto-opti.prng                      :as prng]
   [auto-opti.prng.impl.test-suites     :as test-suite]
   [auto-opti.prng.impl.xoroshiro128-js :as sut]
   [auto-opti.prng.stateful             :as stateful]
   [cljs.test                           :refer          [deftest is testing]
                                        :include-macros true]))

(def uuid-1 #uuid "550e8400-e29b-41d4-a716-446655440000")
(def uuid-2 #uuid "6ba7b810-9dad-11d1-80b4-00c04fd430c8")

;; Reference values from the C implementation (xoroshiro128+, a=24 b=16 c=37)
;; for the raw initial state s[0]=1, s[1]=2. Shared with the JVM
;; `reference-implementation-test`.
(def reference-values-state-1-2
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

(defn- pad8 [s] (str (subs "00000000" (count s)) s))

(defn- raw-str
  "Unsigned decimal string of the raw 64-bit value held in lanes hi/lo."
  [hi lo]
  (let [hex (str "0x"
                 (pad8 (.toString (unsigned-bit-shift-right hi 0) 16))
                 (pad8 (.toString (unsigned-bit-shift-right lo 0) 16)))]
    (.toString (js/BigInt hex))))

(deftest core-matches-c-reference
  (testing "raw xoroshiro128+ output matches the C reference for state [1, 2]"
    (let [st #js [0 1 0 2 0 0]
          actual (vec (repeatedly 20 (fn [] (sut/next! st) (raw-str (aget st 4) (aget st 5)))))]
      (is (= reference-values-state-1-2 actual)
          "JS core should match the canonical C reference bit for bit"))))

;; Shared stateful-protocol lifecycle suite (range, determinism, reset,
;; duplicate, jump, ...). Same suite as the JVM fast variants.
(deftest prng-protocol (test-suite/run-prng-protocol-tests sut/make uuid-1 uuid-2))

(deftest uniformity
  ;; With 10 equiprobable buckets at n=10000 the expected coefficient of
  ;; variation is already ~3%, so 5 leaves room for normal sampling noise while
  ;; still catching gross non-uniformity (which yields cv well above 10).
  (testing "Uniformity coefficient of variation stays below 5"
    (let [p (sut/make uuid-1)]
      (is (nil? (test-suite/dstb-uniformity (stateful/as-ints p 10000 0 10) 5))))))

(deftest registered-in-public-api
  (testing ":xoroshiro128-js is reachable through the public prng registry"
    (let [p (prng/prng #::opti{:prng-name :xoroshiro128-js
                               :seed uuid-1})]
      (is (some? p))
      (is (every? #(and (>= % 5) (< % 15)) (prng/as-ints p 100 5 15))))))
