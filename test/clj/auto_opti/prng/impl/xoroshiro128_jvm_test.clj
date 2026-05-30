(ns auto-opti.prng.impl.xoroshiro128-jvm-test
  (:require
   [auto-opti.prng.advanced-tests        :refer [run-advanced-quality-tests]]
   [auto-opti.prng.impl.test-suites      :as test-suite]
   [auto-opti.prng.impl.xoroshiro128-jvm :as sut]
   [auto-opti.prng.stateful              :as opt-prng-stateful]
   [clojure.test                         :refer [deftest is testing]]
   [criterium.core                       :as crit]))

(def uuid-1 #uuid "550e8400-e29b-41d4-a716-446655440000")
(def uuid-2 #uuid "6ba7b810-9dad-11d1-80b4-00c04fd430c8")

;; ============================================================================
;; Quality tests using the map-based shared test suites
;; ============================================================================

(deftest test-xoroshiro128-quality (test-suite/run-quality-tests sut/make-generators uuid-1 uuid-2))

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

(deftest test-uuid->state
  (testing "uuid->state produces valid state"
    (let [[s0 s1] (sut/uuid->state uuid-1)]
      (is (number? s0) "s0 should be a number")
      (is (number? s1) "s1 should be a number")
      (is (not (and (zero? s0) (zero? s1))) "State should not be all zeros"))
    (let [state1 (sut/uuid->state uuid-1)
          state2 (sut/uuid->state uuid-2)]
      (is (not= (vec state1) (vec state2)) "Different UUIDs should produce different states"))
    (let [[s0 s1] (-> #uuid "00000000-0000-0000-0000-000000000000"
                      sut/uuid->state)]
      (is (or (not (zero? s0)) (not (zero? s1))) "All-zero UUID should produce non-zero state"))))

(deftest test-xoroshiro-next
  (testing "xoroshiro-next! generates sequence correctly"
    (let [state (long-array [1 2])
          r1 (sut/xoroshiro-next! state)]
      (is (= 3 r1) "First result should be 1 + 2 = 3")
      (is (not= 1 (aget state 0)) "State should change after call")
      (is (not= 2 (aget state 1)) "State should change after call"))
    (let [state1 (long-array [12345 67890])
          state2 (long-array [12345 67890])
          r1a (sut/xoroshiro-next! state1)
          r2a (sut/xoroshiro-next! state2)]
      (is (= r1a r2a) "Same state should produce same result")
      (is (= (aget state1 0) (aget state2 0)) "States should evolve identically")
      (is (= (aget state1 1) (aget state2 1)) "States should evolve identically"))))

(deftest test-sample-output
  (testing "Display sample output for manual verification"
    (let [prng (sut/make-generators uuid-1)
          gen ((:next-int prng) 0 1000)
          samples (vec (repeatedly 20 gen))]
      (println "Xoroshiro128+ samples (0-1000):" samples)
      (is (= 20 (count samples))))))

;; ============================================================================
;; Benchmarks - excluded from the standard run (`-e :benchmark`), machine
;; dependent. Run explicitly with `-i :benchmark`.
;; ============================================================================

(deftest ^:benchmark xoro-time-efficiency
  (let [prng (sut/make-generators uuid-1)]
    (is (< (let [gen ((:next-raw prng))
                 dur (-> (crit/benchmark (gen) {:verbose true})
                         :mean
                         first)]
             (println "Xoro mean time for raw:" dur)
             dur)
           1.01E-8)
        "Testing raw generation")
    (is (< (let [gen ((:next-int prng) 0 1000)
                 dur (-> (crit/benchmark (gen) {:verbose true})
                         :mean
                         first)]
             (println "Xoro mean time for integer: " dur)
             dur)
           1.4387971312235925E-5)
        "Testing integer generation")
    (is (< (let [gen ((:next-double prng) 0 1000)
                 dur (-> (crit/benchmark (gen) {:verbose true})
                         :mean
                         first)]
             (println "Xoro mean double:" dur)
             dur)
           1.01E-8)
        "Testing double generation")))
