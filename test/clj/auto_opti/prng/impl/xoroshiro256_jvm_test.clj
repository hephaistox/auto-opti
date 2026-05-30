(ns auto-opti.prng.impl.xoroshiro256-jvm-test
  (:require
   [auto-opti                            :as-alias opti]
   [auto-opti.prng                       :as prng]
   [auto-opti.prng.advanced-tests        :refer [run-advanced-quality-tests]]
   [auto-opti.prng.impl.test-suites      :as test-suite]
   [auto-opti.prng.impl.xoroshiro256-jvm :as sut]
   [clojure.test                         :refer [deftest is testing]]))

(def uuid-1 #uuid "550e8400-e29b-41d4-a716-446655440000")
(def uuid-2 #uuid "6ba7b810-9dad-11d1-80b4-00c04fd430c8")

;; ============================================================================
;; Quality tests using the map-based shared test suites
;; ============================================================================

(deftest test-xoroshiro256-quality (test-suite/run-quality-tests sut/make-generators uuid-1 uuid-2))

(deftest xoro-advanced-test (run-advanced-quality-tests sut/make-generators uuid-1))

;; ============================================================================
;; PRNG protocol (the registry-facing `make`) - shared lifecycle suite
;; ============================================================================

(deftest prng-protocol-test (test-suite/run-prng-protocol-tests sut/make uuid-1 uuid-2))

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
