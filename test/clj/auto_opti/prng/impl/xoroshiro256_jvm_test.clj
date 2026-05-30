(ns auto-opti.prng.impl.xoroshiro256-jvm-test
  (:require
   [auto-opti                            :as-alias opti]
   [auto-opti.prng                       :as prng]
   [auto-opti.prng.advanced-tests        :refer [run-advanced-quality-tests]]
   [auto-opti.prng.impl.test-suites      :as test-suite]
   [auto-opti.prng.impl.xoroshiro256-jvm :as sut]
   [auto-opti.prng.stateful              :as opt-prng-stateful]
   [clojure.test                         :refer [deftest is testing]]))

(def uuid-1 #uuid "550e8400-e29b-41d4-a716-446655440000")
(def uuid-2 #uuid "6ba7b810-9dad-11d1-80b4-00c04fd430c8")

;; ============================================================================
;; Quality tests using the map-based shared test suites
;; ============================================================================

(deftest test-xoroshiro256-quality (test-suite/run-quality-tests sut/make-generators uuid-1 uuid-2))

(deftest xoro-advanced-test
  (is (run-advanced-quality-tests sut/make-generators uuid-1)
      "Are chi2 tests, runs, gap, serial, collision and poker test passing"))

;; ============================================================================
;; PRNG protocol (the registry-facing `make`)
;; ============================================================================

(deftest prng-protocol-test
  (testing "rnd-int stays in range"
    (let [p (sut/make uuid-1)]
      (is (every? #(and (>= % 0) (< % 100))
                  (repeatedly 1000 #(opt-prng-stateful/rnd-int p 0 100))))))
  (testing "rnd-double stays in range"
    (let [p (sut/make uuid-1)]
      (is (every? #(and (>= % 0.0) (< % 1.0))
                  (repeatedly 1000 #(opt-prng-stateful/rnd-double p 0.0 1.0))))))
  (testing "determinism - same seed, same sequence"
    (let [seq-of (fn []
                   (let [p (sut/make uuid-1)]
                     (vec (repeatedly 50 #(opt-prng-stateful/rnd-int p 0 1000)))))]
      (is (= (seq-of) (seq-of)))))
  (testing "different seeds diverge"
    (let [p1 (sut/make uuid-1)
          p2 (sut/make uuid-2)]
      (is (not= (vec (repeatedly 50 #(opt-prng-stateful/rnd-int p1 0 1000)))
                (vec (repeatedly 50 #(opt-prng-stateful/rnd-int p2 0 1000)))))))
  (testing "uuid-seed round-trips" (is (= uuid-1 (opt-prng-stateful/uuid-seed (sut/make uuid-1)))))
  (testing "reset replays from the seed"
    (let [p (sut/make uuid-1)
          before (vec (repeatedly 10 #(opt-prng-stateful/rnd-int p 0 1000)))
          _ (dotimes [_ 100] (opt-prng-stateful/rnd-int p 0 1000))
          after (do (opt-prng-stateful/reset p)
                    (vec (repeatedly 10 #(opt-prng-stateful/rnd-int p 0 1000))))]
      (is (= before after))))
  (testing "duplicate starts a fresh generator at the seed"
    (let [p (sut/make uuid-1)
          _ (dotimes [_ 100] (opt-prng-stateful/rnd-int p 0 1000))
          d (opt-prng-stateful/duplicate p)
          fresh (sut/make uuid-1)]
      (is (= (vec (repeatedly 10 #(opt-prng-stateful/rnd-int d 0 1000)))
             (vec (repeatedly 10 #(opt-prng-stateful/rnd-int fresh 0 1000)))))))
  (testing "jump moves to a different stream"
    (let [jumped (opt-prng-stateful/jump (sut/make uuid-1))
          fresh (sut/make uuid-1)]
      (is (not= (vec (repeatedly 10 #(opt-prng-stateful/rnd-int jumped 0 1000000)))
                (vec (repeatedly 10 #(opt-prng-stateful/rnd-int fresh 0 1000000))))))))

;; ============================================================================
;; Implementation-specific tests
;; ============================================================================

(deftest test-xoroshiro-next
  (testing "xoroshiro-next! returns s0 + s3 and evolves the state"
    (let [state (long-array [1 2 3 4])
          r1 (sut/xoroshiro-next! state)]
      (is (= 5 r1) "First result should be s0 + s3 = 1 + 4 = 5")
      (is (not= 1 (aget state 0)) "State should change after call"))
    (let [state1 (long-array [1 2 3 4])
          state2 (long-array [1 2 3 4])]
      (is (= (sut/xoroshiro-next! state1) (sut/xoroshiro-next! state2)) "Same state, same result")
      (is (= (vec state1) (vec state2)) "States should evolve identically"))))

(deftest test-uuid->state
  (testing "uuid->state produces a non-zero 256-bit state"
    (let [state (sut/uuid->state uuid-1)]
      (is (= 4 (count state)) "Should have four words")
      (is (not-every? zero? state) "State should not be all zeros"))
    (is (not= (vec (sut/uuid->state uuid-1)) (vec (sut/uuid->state uuid-2)))
        "Different UUIDs should produce different states")))

;; ============================================================================
;; Public registry
;; ============================================================================

(deftest registered-in-public-api
  (testing ":xoroshiro256-jvm is reachable through the public prng registry"
    (let [p (prng/prng #::opti{:prng-name :xoroshiro256-jvm
                               :seed uuid-1})]
      (is (some? p))
      (is (every? #(and (>= % 5) (< % 15)) (prng/as-ints p 100 5 15))))))
