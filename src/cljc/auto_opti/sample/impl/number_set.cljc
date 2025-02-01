(ns auto-opti.sample.impl.number-set
  "Functions manipulating set of numbers for probalistic analyzis.

  Some functions are inspired from `https://github.com/MastodonC/kixi.stats`.
  and optimization tips about `http://hugoduncan.org/criterium/0.4/uberdoc.html`."
  (:refer-clojure :exclude [min max sqrt abs exp])
  (:require
   [auto-opti.maths :as opt-maths]))

(defn average
  "Compute the average of the collection `coll`.

  Execution time mean : 86.310310 µs, std-dev 546.868164 ns."
  [coll]
  (when-let [n (count coll)] (when (pos? n) (/ (reduce + 0 coll) n))))

(defn variance
  "Variance of a collection `coll`.

  Execution time mean : 76.108405 µs, std-dev = 1.321352 ns"
  [coll]
  (let [[sum-of-squares sum-of n]
        (reduce (fn [[^double sum-of-squares ^double sum-of k] ^double elt]
                  [(+ sum-of-squares (opt-maths/square elt)) (+ sum-of elt) (inc k)])
                [0 0 0]
                coll)]
    (when-not (or (nil? n) (zero? n)) (- (/ sum-of-squares n) (opt-maths/square (/ sum-of n))))))

(defn standard-deviation
  "Returns the standard deviation of the collection `coll`."
  [coll]
  (when-let [v (variance coll)] (when (some? v) (opt-maths/sqrt v))))

(defn median
  "Returns the median of the collection `coll`."
  [coll]
  (let [sorted (sort coll)
        n (count sorted)
        i (bit-shift-right n 1)]
    (if (odd? n) (nth sorted i) (average [(nth sorted (dec i)) (nth sorted i)]))))

(defn midrange
  "Find the midrange of the collection `coll`."
  [coll]
  (when-not (empty? coll)
    (average [(reduce opt-maths/min-double coll) (reduce opt-maths/max-double coll)])))

(defn midrange-double
  "Find the midrange of the collection `coll`."
  [coll]
  (when-not (empty? coll)
    (average [(reduce opt-maths/min-double coll) (reduce opt-maths/max-double coll)])))

(defn midrange-long
  "Find the midrange of the collection `coll`."
  [coll]
  (when-not (empty? coll)
    (average [(reduce opt-maths/min-long coll) (reduce opt-maths/max-long coll)])))

(defn erf
  "erf polynomial approximation of `x`.  Maximum error is `1.5e-7.`
  Handbook of Mathematical Functions: with Formulas, Graphs, and Mathematical Tables. Milton Abramowitz (Editor), Irene A. Stegun (Editor), 7.1.26"
  [x]
  (let [x (double x)
        sign (opt-maths/signum x)
        x (opt-maths/abs x)
        a [1.061405429 -1.453152027 1.421413741 -0.284496736 0.254829592 0.0]
        p 0.3275911
        t (/ (+ 1.0 (* p x)))
        value (- 1.0 (* (opt-maths/polynomial-value t a) (opt-maths/exp (- (* x x)))))]
    (* sign value)))
