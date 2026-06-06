(ns auto-opti.prng.advanced-tests
  "Advanced statistical tests for PRNG quality.
   These tests go beyond basic functionality to verify statistical properties."
  (:require
   [clojure.test :refer [is testing]]))

;; ============================================================================
;; Chi-Square Test for Uniformity
;; ============================================================================

(defn chi-square-test
  "Chi-square test for uniform distribution.
   Returns [chi-square-statistic critical-value-95% passed?]"
  [make-fn uuid-or-seed]
  (testing "Chi-square test for uniformity"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 100)
          n-samples 10000
          n-buckets 100
          expected-per-bucket (/ n-samples n-buckets)
          samples (repeatedly n-samples gen)
          observed (frequencies samples)
          ;; Calculate chi-square statistic
          chi-square (reduce (fn [sum bucket]
                               (let [obs (get observed bucket 0)
                                     diff (- obs expected-per-bucket)]
                                 (+ sum (/ (* diff diff) expected-per-bucket))))
                             0.0
                             (range n-buckets))
          ;; Critical value for 99 degrees of freedom at 95% confidence
          critical-value-95 124.342]
      (println "Chi-square: " chi-square " (critical: " critical-value-95 ")")
      (is (< chi-square critical-value-95)
          "Chi-square statistic should be below critical value for uniform distribution"))))

;; ============================================================================
;; Runs Test (above/below median)
;; ============================================================================

(defn runs-test
  "Test for independence - counts runs above and below median.
   Too few runs = positive correlation, too many = negative correlation"
  [make-fn uuid-or-seed]
  (testing "Runs test for independence"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 1000)
          n-samples 1000
          samples (vec (repeatedly n-samples gen))
          median (nth (sort samples) (quot n-samples 2))
          ;; Convert to sequence of above(1) / below(0) median
          binary (map #(if (> % median) 1 0) samples)
          ;; Count runs (sequences of same value)
          runs (reduce (fn [[count prev] curr] (if (= curr prev) [count curr] [(inc count) curr]))
                       [1 (first binary)]
                       (rest binary))
          n-runs (first runs)
          n1 (count (filter #(= % 1) binary))
          n0 (- n-samples n1)
          ;; Expected runs and standard deviation for random sequence
          expected-runs (+ 1 (/ (* 2 n1 n0) n-samples))
          std-dev (Math/sqrt (/ (* 2 n1 n0 (- (* 2 n1 n0) n-samples))
                                (* n-samples n-samples (dec n-samples))))
          ;; Z-score (should be within -2 to 2 for 95% confidence)
          z-score (/ (- n-runs expected-runs) std-dev)]
      (is (and (> z-score -2.5) (< z-score 2.5))
          "Z-score should indicate independence (between -2.5 and 2.5)"))))

;; ============================================================================
;; Gap Test
;; ============================================================================

(defn gap-test
  "Test gaps between occurrences of values in a specific range.
   Tests if gaps follow geometric distribution as expected for random sequences."
  [make-fn uuid-or-seed]
  (testing "Gap test - distribution of gaps between target values"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 100)
          target-range [0 10] ;; Looking for values in [0, 10)
          n-samples 5000
          samples (repeatedly n-samples gen)
          ;; Find gaps between values in target range
          gaps (loop [remaining samples
                      current-gap 0
                      gaps []]
                 (if (empty? remaining)
                   gaps
                   (let [val (first remaining)]
                     (if (and (>= val (first target-range)) (< val (second target-range)))
                       (recur (rest remaining) 0 (conj gaps current-gap))
                       (recur (rest remaining) (inc current-gap) gaps)))))
          ;; Group gaps into buckets: 0-4, 5-9, 10-14, 15-19, 20+
          gap-freqs (frequencies (map #(cond
                                         (< % 5) 0
                                         (< % 10) 1
                                         (< % 15) 2
                                         (< % 20) 3
                                         :else 4)
                                      gaps))
          total-gaps (count gaps)]
      ;; Should find a reasonable number of gaps
      (is (> total-gaps 100) "Should find enough gaps to analyze")
      ;; Most gaps should be in lower buckets (geometric distribution)
      (is (> (get gap-freqs 0 0) (get gap-freqs 4 0))
          "Should have more short gaps than long gaps"))))

;; ============================================================================
;; Serial Correlation Test
;; ============================================================================

(defn serial-correlation-test
  "Test for correlation between consecutive values.
   Correlation should be near 0 for independent random values."
  [make-fn uuid-or-seed]
  (testing "Serial correlation test"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 1000)
          n-samples 1000
          samples (vec (repeatedly n-samples gen))
          ;; Calculate mean
          mean (/ (reduce + samples) n-samples)
          ;; Calculate serial correlation (lag-1)
          numerator (reduce +
                            (map (fn [i] (* (- (samples i) mean) (- (samples (inc i)) mean)))
                                 (range (dec n-samples))))
          denominator (reduce + (map (fn [x] (* (- x mean) (- x mean))) samples))
          correlation (/ numerator denominator)]
      (is (and (> correlation -0.1) (< correlation 0.1))
          "Serial correlation should be close to 0 (between -0.1 and 0.1)"))))

;; ============================================================================
;; Collision Test
;; ============================================================================

(defn collision-test
  "Test for unexpected collisions (birthday paradox).
   With n samples from range m, expected unique values ≈ m(1 - e^(-n/m))"
  [make-fn uuid-or-seed]
  (testing "Collision test - birthday paradox"
    (let [prng (make-fn uuid-or-seed)
          range-size 100000
          gen ((:next-int prng) 0 range-size)
          n-samples 10000
          samples (repeatedly n-samples gen)
          unique-count (count (set samples))
          ;; Expected unique values: m * (1 - e^(-n/m))
          ratio (/ n-samples range-size)
          expected-unique (* range-size (- 1 (Math/exp (- ratio))))
          ;; Allow 5% deviation
          lower-bound (* expected-unique 0.95)
          upper-bound (* expected-unique 1.05)]
      (is (and (>= unique-count lower-bound) (<= unique-count upper-bound))
          "Number of unique values should match birthday paradox prediction"))))

;; ============================================================================
;; Poker Test (patterns in sequences)
;; ============================================================================

(defn poker-test
  "Poker test - analyze patterns in sequences of 5 consecutive values.
   Tests if patterns occur with expected frequencies."
  [make-fn uuid-or-seed]
  (testing "Poker test - pattern analysis"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 10) ;; Use 10 values (like playing cards)
          n-hands 2000
          ;; Generate hands of 5 values each
          hands (repeatedly n-hands #(repeatedly 5 gen))
          ;; Classify each hand
          classify-hand (fn [hand]
                          (let [freqs (vals (frequencies hand))
                                sorted-freqs (sort > freqs)]
                            (cond
                              (= sorted-freqs [5]) :five-kind
                              (= sorted-freqs [4 1]) :four-kind
                              (= sorted-freqs [3 2]) :full-house
                              (= sorted-freqs [3 1 1]) :three-kind
                              (= sorted-freqs [2 2 1]) :two-pair
                              (= sorted-freqs [2 1 1 1]) :one-pair
                              :else :all-different)))
          hand-types (frequencies (map classify-hand hands))
          ;; Expected probabilities (approximate for 10 values)
          ;; All different should be most common (~30%)
          all-diff-pct (/ (get hand-types :all-different 0) n-hands)]
      (is (> all-diff-pct 0.25) "Should have reasonable frequency of all-different hands")
      (is (< all-diff-pct 0.35) "All-different hands shouldn't be too frequent"))))

;; ============================================================================
;; Run all advanced tests
;; ============================================================================

(defn run-advanced-quality-tests
  "Run all advanced statistical tests for PRNG quality"
  [make-fn uuid-or-seed]
  (chi-square-test make-fn uuid-or-seed)
  (runs-test make-fn uuid-or-seed)
  (gap-test make-fn uuid-or-seed)
  (serial-correlation-test make-fn uuid-or-seed)
  (collision-test make-fn uuid-or-seed)
  (poker-test make-fn uuid-or-seed))

;; ============================================================================
;; Reference Implementation Test
;; ============================================================================

(defn test-against-reference
  "Compare output against known reference implementation.
   This requires having reference values from the C implementation."
  [make-fn uuid-or-seed expected-first-10]
  (testing "Compare against reference implementation"
    (let [prng (make-fn uuid-or-seed)
          gen ((:next-int prng) 0 1000000)
          actual-first-10 (vec (repeatedly 10 gen))]
      (println "Expected:" expected-first-10)
      (println "Actual:  " actual-first-10)
      (is (= expected-first-10 actual-first-10) "Should match reference implementation output"))))

(comment
  ;; Example usage - these suites expect the map-based generator API
  ;; (`make-generators`), not the `PRNG`-returning `make`.
  (require '[auto-opti.prng.impl.xoroshiro128-jvm :as xoro])
  (run-advanced-quality-tests xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (chi-square-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (runs-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (gap-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (serial-correlation-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (collision-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  (poker-test xoro/make-generators #uuid "550e8400-e29b-41d4-a716-446655440000")
  ;; For even more rigorous testing, you can:
  ;; 1. Run TestU01 SmallCrush/Crush/BigCrush (requires C wrapper)
  ;; 2. Run PractRand (requires generating large binary files)
  ;; 3. Run Dieharder test suite
  ;; 4. Compare bit-level output with reference C implementation
)
