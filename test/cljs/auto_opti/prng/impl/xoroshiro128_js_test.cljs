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

(deftest range-and-determinism
  (testing "rnd-int stays in range"
    (let [p (sut/make uuid-1)]
      (is (every? #(and (>= % 0) (< % 100)) (repeatedly 1000 #(stateful/rnd-int p 0 100))))))
  (testing "rnd-double stays in range"
    (let [p (sut/make uuid-1)]
      (is (every? #(and (>= % 0.0) (< % 1.0)) (repeatedly 1000 #(stateful/rnd-double p 0.0 1.0))))))
  (testing "determinism - same seed, same sequence"
    (let [seq-of (fn []
                   (let [p (sut/make uuid-1)] (vec (repeatedly 50 #(stateful/rnd-int p 0 1000)))))]
      (is (= (seq-of) (seq-of)))))
  (testing "different seeds diverge"
    (let [p1 (sut/make uuid-1)
          p2 (sut/make uuid-2)]
      (is (not= (vec (repeatedly 50 #(stateful/rnd-int p1 0 1000)))
                (vec (repeatedly 50 #(stateful/rnd-int p2 0 1000))))))))

(deftest uniformity
  ;; With 10 equiprobable buckets at n=10000 the expected coefficient of
  ;; variation is already ~3%, so 5 leaves room for normal sampling noise while
  ;; still catching gross non-uniformity (which yields cv well above 10).
  (testing "Uniformity coefficient of variation stays below 5"
    (let [p (sut/make uuid-1)]
      (is (nil? (test-suite/dstb-uniformity (stateful/as-ints p 10000 0 10) 5))))))

(deftest protocol-lifecycle
  (testing "uuid-seed round-trips" (is (= uuid-1 (stateful/uuid-seed (sut/make uuid-1)))))
  (testing "reset replays from the seed"
    (let [p (sut/make uuid-1)
          before (vec (repeatedly 10 #(stateful/rnd-int p 0 1000)))
          _ (dotimes [_ 100] (stateful/rnd-int p 0 1000))
          after (do (stateful/reset p) (vec (repeatedly 10 #(stateful/rnd-int p 0 1000))))]
      (is (= before after))))
  (testing "duplicate starts a fresh generator at the seed"
    (let [p (sut/make uuid-1)
          _ (dotimes [_ 100] (stateful/rnd-int p 0 1000))
          d (stateful/duplicate p)
          fresh (sut/make uuid-1)]
      (is (= (vec (repeatedly 10 #(stateful/rnd-int d 0 1000)))
             (vec (repeatedly 10 #(stateful/rnd-int fresh 0 1000)))))))
  (testing "jump moves to a different stream"
    (let [jumped (stateful/jump (sut/make uuid-1))
          fresh (sut/make uuid-1)]
      (is (not= (vec (repeatedly 10 #(stateful/rnd-int jumped 0 1000000)))
                (vec (repeatedly 10 #(stateful/rnd-int fresh 0 1000000))))))))

(deftest registered-in-public-api
  (testing ":xoroshiro128-js is reachable through the public prng registry"
    (let [p (prng/prng #::opti{:prng-name :xoroshiro128-js
                               :seed uuid-1})]
      (is (some? p))
      (is (every? #(and (>= % 5) (< % 15)) (prng/as-ints p 100 5 15))))))
