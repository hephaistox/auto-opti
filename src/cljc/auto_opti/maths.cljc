(ns auto-opti.maths
  "Implements all mathematical basics functions compatible both with clj and cljs compiler and aiming at return the same results."
  (:refer-clojure :exclude [infinite? abs min max mod])
  (:require
   #?(:cljs [cljs.math :as math])
   [xoroshiro128.core :as xoro]))

;; Constants

(def SQRT_2_PI "Square root of twice pi." 2.506628274631000502)

(def LANCZOS_G "The Lanczos constant." (/ 607 128))

; http://www.exploringbinary.com/hexadecimal-floating-point-constants/
(def D-0x1p-53
  #?(:cljs (Math/pow 2 -53)
     :clj (Double/parseDouble "0x1.0p-53")))

(def PI
  "Value of PI based on platform constant value

  According to [maths.com](http://www.math.com/tables/constants/pi.htm), the first values are:
  3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811174502841027019385211055596446229489549303819644288109756659334461284756482337867831652712019091456485669234603486104543266482133936072602491412737245870066063155881748815209209628292540917153643678925903600113305305488204665213841469519415116094330572703657595919530921861173819326117931051185480744623799627495673518857527248912279381830119491298336733624406566430860213949463952247371907021798609437027705392171762931767523846748184676694051320005681271452635608277857713427577896091736371787214684409012249534301465495853710507922796892589235420199561121290219608640344181598136297747713099605187072113499999983729780499510597317328160963185950244594553469083026425223082533446850352619311881710100031378387528865875332083814206171776691473035982534904287554687311595628638823537875937519577818577805321712268066130019278766111959092164201989"
  #?(:clj Math/PI
     :cljs js/Math.PI))

(def HALF_PI "Half  PI" (/ PI 2.0))

(def interop-max-integer
  "Returns same int value that's highest possible and equal for both clj and cljs. For refernce look at Integer/MAX_VALUE clojure interop documentation"
  (- (Math/pow 2 31) 1))

(def interop-min-integer "same as interop-max-integer but for smallest" (- (Math/pow -2 31) 1))

(def infinity-integer
  "Returns infinite for integer."
  #?(:clj Integer/MAX_VALUE
     :cljs js/Infinity))

(def negative-infinity-integer
  "Returns minus infinite for integer."
  #?(:clj Integer/MIN_VALUE
     :cljs js/-Infinity))

(def infinity-long
  "Returns infinite for integer."
  #?(:clj Long/MAX_VALUE
     :cljs js/Infinity))

(def negative-infinity-long
  "Returns minus infinite for integer."
  #?(:clj Long/MIN_VALUE
     :cljs js/-Infinity))

(def infinity
  "Returns infinite for reals."
  #?(:clj Double/POSITIVE_INFINITY
     :cljs js/Infinity))

(def negative-infinity
  "Returns minus infinite for reals."
  #?(:clj Double/NEGATIVE_INFINITY
     :cljs js/-Infinity))

(def nan
  "Returns nan."
  #?(:clj Double/NaN
     :cljs js/NaN))

;; Basic maths functions

(defn signum
  "Returns the sign of `x` for `double` and `float` numbers."
  [x]
  #?(:clj (Math/signum x)
     :cljs (math/signum x)))

(defn abs
  "Absolute value of `x`."
  [x]
  (cond-> x
    (neg? x) -))

(defn abs-int
  "Absolute value of `x`, optimized for `x∊ℕ`."
  #?(:clj ([^Integer x]
           (cond-> x
             (neg? x) -))
     :cljs ([x]
            (cond-> x
              (neg? x) -))))

(defn abs-double
  "Absolute value of `x`, optimized for `x∊ℝ`."
  #?(:clj ([^double x]
           (cond-> x
             (neg? x) -))
     :cljs ([x]
            (cond-> x
              (neg? x) -))))


(defn mod
  "Returns the modulus of dividing numerator n by denominator d."
  [n d]
  #?(:clj (clojure.core/mod n d)
     :cljs (cljs.core/mod n d)))

(defn sqrt
  "Square root of `x`, where `x∊[0;+∞[`."
  [x]
  #?(:clj (Math/sqrt x)
     :cljs (js/Math.sqrt x)))

(defn square "Square of `x`." [x] (* x x))

(defn square-double
  "Square of `x`, optimized for `x` as a double."
  #?(:clj ([^Double x] (* x x))
     :cljs ([x] (* x x))))

(defn square-int
  "Square of `x`, optimized for `x` as an integer."
  #?(:clj ([^Integer x] (* x x))
     :cljs ([x] (* x x))))

(defn pow
  "Power `n` of `x`, where `(x,n)∊ℝ²`."
  [^Double x ^Double n]
  #?(:clj (Math/pow x n)
     :cljs (js/Math.pow x n)))

(defn root
  "Return the `n` root of `x`, where `(x,n)∊ℝ²`."
  #?(:clj ([^Double x ^Double n] (pow x (/ 1.0 n)))
     :cljs ([x n] (pow x (/ 1.0 n)))))

(defn log
  "Returns the neperian logarithm of `x`, where `x∊ℝ`."
  [^Double x]
  #?(:clj (Math/log x)
     :cljs (js/Math.log x)))

(defn log1p
  "Returns `(ln 1+x)` the natural logarithm of `1+x`, where `x∊ℝ`."
  #?(:clj ([^Double x] (Math/log1p x))
     :cljs ([x] (js/Math.log (inc x)))))

(defn exp
  "Returns the exponential of `x`, where `x∊ℝ`."
  [^Double x]
  #?(:clj (Math/exp x)
     :cljs (js/Math.exp x)))

(defn cos
  "Returns the cosinus of `x`, where `x` is a multiple of `PI`."
  [^Double x]
  #?(:clj (Math/cos x)
     :cljs (js/Math.cos x)))

(defn sin
  "Returns the sinus of `x`, where `x` as a multiple of `PI`."
  [^Double x]
  #?(:clj (Math/sin x)
     :cljs (js/Math.sin x)))

(defn tan
  "Returns the tangeant of `x`, where `x` as a multiple of `PI`."
  [^Double x]
  #?(:clj (Math/tan x)
     :cljs (js/Math.tan x)))

(defn atan
  "Returns the arctangeant of `x`, where `x` as a multiple of `PI`."
  [^Double x]
  #?(:clj (Math/atan x)
     :cljs (js/Math.atan x)))

(defn ceil
  "Returns the ceiling of `x` (the upper rounded value)."
  [^Double x]
  #?(:clj (Math/ceil x)
     :cljs (js/Math.ceil x)))

(defn floor
  "Returns the floor of `x` (the lower rounded value)."
  [^Double x]
  #?(:clj (Math/floor x)
     :cljs (js/Math.floor x)))

(defn approx=
  "Returns true for an approximate numerical equality."
  #?(:clj ([^Double e ^Double x ^Double y]
           (cond
             (and (nil? x) (nil? y)) true
             (or (nil? x) (nil? y)) false
             :else (<= (abs (- y x)) e)))
     :cljs ([e x y]
            (cond
              (and (nil? x) (nil? y)) true
              (or (nil? x) (nil? y)) false
              :else (<= (abs (- y x)) e)))))

(defn infinite?
  "Returns true if `x` is infinite."
  [^Double x]
  #?(:clj (Double/isInfinite x)
     :cljs (not (js/isFinite x))))

(defn long->unit-double
  "Returns a unit float (a floating number between 0 and 1), based on the value of `l`."
  #?(:clj ([l] (xoro/long->unit-float l))
     :cljs ([l] (xoro/long->unit-float l))))

(defn interval-affine-fn
  "Returns an affine function with coefficient `a` and `b`, defined in `[x1; x2[`.

  If
  * `x1` if not a number (typically nil), is interprated like an interval starting at -infinite
  * `x2` if not a number (typically nil), is interprated like a non ending interval (infinite)"
  [a b x1 x2 x]
  (cond
    (and (or (not (number? x1)) (<= x1 x)) (or (not (number? x2)) (< x x2))) (+ (* x a) b)
    :else nil))

;; Constants leveraging functions

(def HALF_LOG_2_PI "Half of log of twice PI" (* 0.5 (log (* 2.0 PI))))


;; Min max
(def min-double
  "Like `clojure.core/min,` but transducer and nil-friendly."
  (fn
    ([] infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([^double acc e] (if (nil? e) acc (let [e (double e)] (clojure.core/min acc e))))))

(def max-double
  "Like `clojure.core/max,` but transducer and nil-friendly."
  (fn
    ([] negative-infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([^double acc e] (if (nil? e) acc (let [e (double e)] (clojure.core/max acc e))))))

(def min
  "Like `clojure.core/min,` but transducer and nil-friendly."
  (fn
    ([] infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([acc e] (if (nil? e) acc (clojure.core/min acc e)))))

(def max
  "Like clojure.core/max, but transducer and nil-friendly."
  (fn
    ([] negative-infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([acc e] (if (nil? e) acc (clojure.core/max acc e)))))

(def min-long
  "Like clojure.core/min, but transducer and nil-friendly."
  (fn
    ([] infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([^Long acc e] (if (nil? e) acc (let [e (long e)] (clojure.core/min acc e))))))

(def max-long
  "Like clojure.core/max, but transducer and nil-friendly."
  (fn
    ([] negative-infinity)
    ([acc] (when-not (infinite? acc) acc))
    ([^Long acc e] (if (nil? e) acc (let [e (long e)] (clojure.core/max acc e))))))

(defn clamp
  "Returns the clamped value between `lower` and `upper`."
  [lower upper x]
  (min (max x lower) upper))

(defn clamp-integer
  "Returns the clamped value between `lower` and `upper`, where `(x,n)∊ℕ²`."
  [^Long lower ^Long upper ^Long x]
  (min-long (max-long x lower) upper))

(defn clamp-double
  "Returns the clamped value between `lower` and `upper`, where `x∊ℝ`."
  [^Double lower ^Double upper ^Double x]
  (min-double (max-double x lower) upper))

;; Proportions
(defn- proportion-xf
  [pred]
  (fn [[n d] e]
    (vector (cond-> n
              (pred e) inc)
            (inc d))))

(defn proportion
  "Calculates the proportion of inputs for which `pred` returns true."
  [f coll]
  (let [[n d] (reduce (proportion-xf f) [0 0] coll)] (when (pos? d) (double (/ n d)))))

;; Polynomial
(defn polynomial-value
  "Evaluates a polynomial at the given value `x`, for the `coefficients` given in descending order (so the last element of coefficients is the constant term)."
  [x coefficients]
  (reduce #(+ (* x %1) %2) (first coefficients) (rest coefficients)))
