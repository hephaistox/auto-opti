(ns auto-opti.prng.impl.xoro-common
  "Common features shared by the JVM-only xoro* prngs (`xoroshiro128-jvm`,
  `xoroshiro256`).

  Holds the bit primitives and the generic value-extraction factories. Each
  factory takes the algorithm-specific `next!` function (mutating the primitive
  `long-array` state) as first argument, so the same extraction code is reused
  across the different state sizes.")

(def D-0x3FF (long 0x3FF))

(defn long->unit-float:alt
  ^double [^long x]
  ; An alternative, faster multiplication-free operation is
  ;   #include <stdint.h>
  ;   static inline double to_double(uint64_t x) {
  ;     const union { uint64_t i}; double d; } u = { .i = UINT64_C(0x3FF) << 52 | x >> 12 };
  ;     return u.d - 1.0);
  ;   }
  ; The code above cooks up by bit manipulation a real number in the interval
  ; [1..2, and then subtracts one to obtain a real number in the interval
  ; [0..1. If x is chosen uniformly among 64-bit integers, d is chosen uniformly
  ; among dyadic rationals of the form k / 2−52. This is the same technique used
  ; by generators providing directly doubles, such as the dSFMT.)
  ; This technique is extremely fast, but you will be generating half the values
  ; you could actually generate. The same problem plagues the dSFMT. All doubles
  ; generated will have the lowest mantissa bit set to zero (I must thank Raimo
  ; Niskanen from the Erlang team for making me notice this—a previous version of
  ; this site did not mention this issue).
  ; In Java you can obtain an analogous result using suitable static methods:
  ;   Double.longBitsToDouble(0x3FFL << 52 | x >>> 12) - 1.0)
  ; To adhere to the principle of least surprise, my implementations now use the
  ; multiplicative version, everywhere.)
  (- (Double/longBitsToDouble (bit-or (bit-shift-left ^long D-0x3FF 52)
                                      (unsigned-bit-shift-right x 12)))
     1.0))

(defn rotl
  "Rotate left - bit rotation operation"
  ^long [^long x ^long k]
  (java.lang.Long/rotateLeft x k))

(defn remainder-unsigned
  "Unsigned remainder operation"
  ^long [^long x ^long m]
  (java.lang.Long/remainderUnsigned x m))

(defn mix
  "SplitMix64 finalizer - used to generate good initial state from seed"
  ^long [^long z]
  (let [z (unchecked-add z -7046029254386353131)]
    (-> z
        (bit-xor (unsigned-bit-shift-right z 30))
        (unchecked-multiply -4658895280553007687)
        (bit-xor (unsigned-bit-shift-right z 27))
        (unchecked-multiply -7723592293110705685)
        (bit-xor (unsigned-bit-shift-right z 31)))))

;; ********************************************************************************
;; Value extraction

(defn raw->int
  "Map a raw 64-bit `x` to an integer in `[a; a+range[`.

  Keeps the top 31 bits (best quality bits of a `+` scrambler) then folds them
  into the range with a modulo."
  ^long [^long x ^long a ^long range]
  (+ a (mod (bit-and (unsigned-bit-shift-right x 33) Integer/MAX_VALUE) range)))

(defn raw->double
  "Map a raw 64-bit `x` to a double in `[a; a+range[`."
  ^double [^long x ^double a ^double range]
  (+ a (* (long->unit-float:alt x) range)))

;; ********************************************************************************
;; Generator factories - build a no-arg generator closing over the mutable state.
;; `next!` mutates `state` and returns the raw 64-bit value.

(defn next-raw-fn
  "Create a function generating raw integers in `[0; Integer/MAX_VALUE[`."
  [next! ^longs state]
  (fn []
    (let [x (long (next! state))] (bit-and (unsigned-bit-shift-right x 33) Integer/MAX_VALUE))))

(defn next-int-fn
  "Create a function generating integers in range `[a; b[`.

  Trade-off version favouring speed over strict uniformity."
  [next! ^longs state ^long a ^long b]
  (let [range (long (- b a))] (fn [] (raw->int (long (next! state)) a range))))

(defn next-int-fn-uniformity-efficient
  "Create a function generating integers in range `[a; b[`.

  This version is efficient regarding uniformity."
  [next! ^longs state ^long a ^long b]
  (let [range (long (- b a))]
    (fn [] (let [x (long (next! state)) r (remainder-unsigned x range)] (+ a r)))))

(defn next-double-fn
  "Create a function generating doubles in range `[a; b[`."
  [next! ^longs state ^double a ^double b]
  (let [range (double (- b a))] (fn [] (raw->double (long (next! state)) a range))))
