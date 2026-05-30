(ns auto-opti.prng.impl.test-suites
  "Prng test suites."
  {:no-doc true}
  (:require
   [auto-opti.sample :as opt-sample]
   [clojure.test     :refer [is testing]]))

(defn dstb-uniformity
  "If the coefficient of variation (i.e. `cv`) is lower than `max-cv`, returns nil.
  Otherwise, return a map with the coefficient of variation of the collection."
  [coll max-cv]
  (let [freq (frequencies coll)
        std-dev (opt-sample/standard-deviation (vals freq))
        avg (opt-sample/average (vals freq))
        cv (* 100.0 (/ std-dev avg))]
    (when (>= cv max-cv)
      {:std-dev std-dev
       :freq freq
       :avg avg
       :max-cv max-cv
       :cv (* 100.0 (/ std-dev avg))})))

(defn test-basic-functionality
  "Test basic PRNG functionality"
  [make-fn uuid-or-seed]
  (testing "Basic PRNG functionality"
    (let [prng (make-fn uuid-or-seed)]
      (is (map? prng) "Should return a map")
      (is (contains? prng :state) "Should have :state")
      (is (contains? prng :seed) "Should have :seed")
      (is (contains? prng :meta) "Should have :meta")
      (is (contains? (:meta prng) :period) "Meta should have :period")
      (is (contains? (:meta prng) :platform) "Meta should have :platform")
      (is (contains? prng :next-double) "Should have :next-double")
      (is (contains? prng :next-raw) "Should have :next-raw")
      (is (contains? prng :next-int) "Should have :next-int")
      (is (contains? prng :next-intu) "Should have :next-intu")
      (let [gen ((:next-int prng) 0 100)
            samples (repeatedly 10 gen)]
        (is (every? #(and (>= % 0) (< % 100)) samples) "Generated values should be in range")))))

(defn test-range-constraints
  "Test that values stay within specified ranges"
  [make-fn uuid-or-seed]
  (testing "Range constraints"
    ;; Test case 1: Range [0, 10)
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 10)
          samples (repeatedly 100 gen)]
      (is (every? #(and (>= % 0) (< % 10)) samples) "All values should be in [0, 10) range"))
    ;; Test case 2: Range [50, 100)
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 50 100)
          samples (repeatedly 100 gen)]
      (is (every? #(and (>= % 50) (< % 100)) samples) "All values should be in [50, 100) range"))
    ;; Test case 3: Single value range [5, 6)
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 5 6)
          samples (repeatedly 10 gen)]
      (is (every? #(= % 5) samples) "Single value range should always return that value"))
    ;; Test case 4: Distribution check (rough)
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 100)
          samples (repeatedly 10000 gen)
          min-val (apply min samples)
          max-val (apply max samples)]
      (is (< min-val 10) "Should generate some low values")
      (is (> max-val 90) "Should generate some high values"))))

(defn test-uniformity
  "Test that generated values are uniformly distributed"
  [make-fn uuid-or-seed]
  (testing "Uniformity - values should be roughly equally distributed"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 10)
          samples (frequencies (repeatedly 10000 gen))
          counts (vals samples)]
      ;; Each bucket (0-9) should have roughly 1000 samples
      ;; Allow 20% deviation (800-1200)
      (is (every? #(and (>= % 800) (<= % 1200)) counts) "Distribution should be roughly uniform"))))

(defn test-no-short-cycles
  "Test that PRNG doesn't repeat too quickly"
  [make-fn uuid-or-seed]
  (testing "No short cycles - should generate diverse values"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 100000) ;; Larger range to avoid filling it completely
          ;; Generate 10000 values and check for uniqueness
          samples (set (repeatedly 10000 gen))]
      ;; Should have high uniqueness (at least 95% unique)
      (is (>= (count samples) 9400) "Should generate mostly unique values over 10k samples"))))

(defn test-determinism
  "Test that same seed produces same sequence"
  [make-fn uuid-or-seed]
  (testing "Determinism - same seed produces same sequence"
    (let [prng1 (make-fn uuid-or-seed)
          prng2 (make-fn uuid-or-seed)
          gen1 ((:next-int prng1) 0 1000)
          gen2 ((:next-int prng2) 0 1000)
          seq1 (vec (repeatedly 100 gen1))
          seq2 (vec (repeatedly 100 gen2))]
      (is (= seq1 seq2) "Same seed should produce identical sequences"))))

(defn test-different-seeds
  "Test that different seeds produce different sequences"
  [make-fn uuid-or-seed-1 uuid-or-seed-2]
  (testing "Different seeds produce different sequences"
    (let [prng1 (make-fn uuid-or-seed-1)
          prng2 (make-fn uuid-or-seed-2)
          gen1 ((:next-int prng1) 0 1000)
          gen2 ((:next-int prng2) 0 1000)
          seq1 (vec (repeatedly 100 gen1))
          seq2 (vec (repeatedly 100 gen2))]
      (is (not= seq1 seq2) "Different seeds should produce different sequences"))))

(defn test-state-independence
  "Test that different PRNG instances don't interfere"
  [make-fn uuid-or-seed]
  (testing "State independence - instances don't interfere"
    (let [prng1 (make-fn uuid-or-seed)
          prng2 (make-fn uuid-or-seed)
          gen1 ((:next-int prng1) 0 100)
          gen2 ((:next-int prng2) 0 100)
          ;; Advance prng1
          val1 (gen1)
          _ (repeatedly 50 gen1)
          ;; prng2 should still start from beginning
          val2 (gen2)]
      (is (= val1 val2) "First value is the same")
      (is (number? val2) "Should generate valid numbers independently"))))

(defn run-quality-tests
  "Run all quality tests for a PRNG implementation"
  [make-fn uuid-or-seed-1 uuid-or-seed-2]
  (test-basic-functionality make-fn uuid-or-seed-1)
  (test-range-constraints make-fn uuid-or-seed-1)
  (test-uniformity make-fn uuid-or-seed-1)
  (test-no-short-cycles make-fn uuid-or-seed-1)
  (test-determinism make-fn uuid-or-seed-1)
  (test-different-seeds make-fn uuid-or-seed-1 uuid-or-seed-2)
  (test-state-independence make-fn uuid-or-seed-1))
