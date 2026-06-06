(ns auto-opti.prng.impl.xoroshiro128-js
  "Xoroshiro128+ PRNG implementation optimized for JavaScript/ClojureScript.

  JavaScript has no 64-bit integers, so the 128-bit state is held as four 32-bit
  lanes in a mutable js array `[s0hi s0lo s1hi s1lo resHi resLo]` and all 64-bit
  arithmetic is emulated with 32-bit operations. This is much faster than the
  portable `:xoroshiro128` (which routes every operation through a long-emulation
  library). Exposed through the registry as `:xoroshiro128-js`.

  The core xoroshiro128+ algorithm matches the canonical C reference bit for bit
  (see `xoroshiro128-js-test`), but the *seeding* is JavaScript-specific, so this
  variant has its own value stream: it is NOT value-compatible with
  `:xoroshiro128` nor `:xoroshiro128-jvm`. For values identical across platforms,
  use `:xoroshiro128`.

  WARNING - not thread-safe (mutates its state in place); use `duplicate` to get
  an independent generator."
  (:require
   [auto-opti.prng.stateful :as opt-prng-stateful]
   [clojure.string          :as str]))

;; ----------------------------------------------------------------------------
;; 64-bit core, on 32-bit lanes. `st` = #js [s0hi s0lo s1hi s1lo resHi resLo].

(defn next!
  "Advance the lane state `st`. Writes the raw 64-bit result into slots 4 (high)
  and 5 (low), and returns `st`. Implements xoroshiro128+ (a=24, b=16, c=37)."
  [st]
  (let [s0hi (aget st 0)
        s0lo (aget st 1)
        s1hi (aget st 2)
        s1lo (aget st 3)
        ;; result = s0 + s1  (64-bit add via unsigned 32-bit halves)
        lo-sum (+ (unsigned-bit-shift-right s0lo 0) (unsigned-bit-shift-right s1lo 0))
        res-lo (bit-or lo-sum 0)
        carry (if (>= lo-sum 4294967296) 1 0)
        res-hi (bit-or (+ s0hi s1hi carry) 0)
        ;; s1 ^= s0
        x1hi (bit-xor s1hi s0hi)
        x1lo (bit-xor s1lo s0lo)
        ;; rotl(s0, 24)
        r0hi (bit-or (bit-shift-left s0hi 24) (unsigned-bit-shift-right s0lo 8))
        r0lo (bit-or (bit-shift-left s0lo 24) (unsigned-bit-shift-right s0hi 8))
        ;; s1 << 16
        sh-hi (bit-or (bit-shift-left x1hi 16) (unsigned-bit-shift-right x1lo 16))
        sh-lo (bit-shift-left x1lo 16)
        ;; new s0 = rotl(s0,24) ^ s1 ^ (s1 << 16)
        n0hi (bit-xor (bit-xor r0hi x1hi) sh-hi)
        n0lo (bit-xor (bit-xor r0lo x1lo) sh-lo)
        ;; new s1 = rotl(s1, 37)  (k > 32, so k2 = 5)
        n1hi (bit-or (bit-shift-left x1lo 5) (unsigned-bit-shift-right x1hi 27))
        n1lo (bit-or (bit-shift-left x1hi 5) (unsigned-bit-shift-right x1lo 27))]
    (aset st 0 n0hi)
    (aset st 1 n0lo)
    (aset st 2 n1hi)
    (aset st 3 n1lo)
    (aset st 4 res-hi)
    (aset st 5 res-lo)
    st))

;; Shared little-endian buffer to reinterpret 64 bits as a double, mirroring the
;; JVM's `Double/longBitsToDouble(0x3FF<<52 | x>>>12) - 1.0` trick.
(def ^:private f64-buf (js/Float64Array. 1))
(def ^:private u32-buf (js/Uint32Array. (.-buffer f64-buf)))

(defn- raw->unit-double
  "Map the raw 64-bit value (hi, lo) to a double in [0; 1)."
  ^number [hi lo]
  (let [mant-hi (unsigned-bit-shift-right hi 12)
        dbl-hi (bit-or 0x3FF00000 mant-hi)
        dbl-lo (bit-or (bit-shift-left hi 20) (unsigned-bit-shift-right lo 12))]
    (aset u32-buf 0 dbl-lo)
    (aset u32-buf 1 dbl-hi)
    (- (aget f64-buf 0) 1.0)))

;; ----------------------------------------------------------------------------
;; Jump - advance by 2^64 steps. JUMP = [0xdf900294d8f554a5 0x170865df4b3201fc].

(def ^:private jump-words #js [0xdf900294 0xd8f554a5 0x170865df 0x4b3201fc])

(defn jump!
  "Advance `st` by 2^64 steps. Mutates and returns `st`."
  [st]
  (let [acc #js [0 0 0 0]]
    (dotimes [w 2]
      (let [whi (aget jump-words (* 2 w))
            wlo (aget jump-words (inc (* 2 w)))]
        (dotimes [b 64]
          (let [bit (if (< b 32)
                      (bit-and (unsigned-bit-shift-right wlo b) 1)
                      (bit-and (unsigned-bit-shift-right whi (- b 32)) 1))]
            (when-not (zero? bit)
              (aset acc 0 (bit-xor (aget acc 0) (aget st 0)))
              (aset acc 1 (bit-xor (aget acc 1) (aget st 1)))
              (aset acc 2 (bit-xor (aget acc 2) (aget st 2)))
              (aset acc 3 (bit-xor (aget acc 3) (aget st 3))))
            (next! st)))))
    (aset st 0 (aget acc 0))
    (aset st 1 (aget acc 1))
    (aset st 2 (aget acc 2))
    (aset st 3 (aget acc 3))
    st))

;; ----------------------------------------------------------------------------
;; Seeding - JavaScript-specific. Each 32-bit lane is derived from a 32-bit word
;; of the UUID, run through the Murmur3 finalizer with a distinct salt.

(defn- fmix32
  "Murmur3 32-bit finalizer - avalanches a 32-bit value."
  [h0]
  (let [h (bit-xor h0 (unsigned-bit-shift-right h0 16))
        h (js/Math.imul h 0x85ebca6b)
        h (bit-xor h (unsigned-bit-shift-right h 13))
        h (js/Math.imul h 0xc2b2ae35)
        h (bit-xor h (unsigned-bit-shift-right h 16))]
    (bit-or h 0)))

(defn- hex->u32 [s] (bit-or (js/parseInt s 16) 0))

(defn uuid->lane-state
  "Derive a non-zero lane state `#js [s0hi s0lo s1hi s1lo 0 0]` from `uuid`."
  [uuid]
  (let [hex (str/replace (str uuid) #"-" "")
        s0hi (fmix32 (bit-xor (hex->u32 (subs hex 0 8)) 0x9e3779b9))
        s0lo (fmix32 (bit-xor (hex->u32 (subs hex 8 16)) 0x243f6a88))
        s1hi (fmix32 (bit-xor (hex->u32 (subs hex 16 24)) 0xb7e15162))
        s1lo (fmix32 (bit-xor (hex->u32 (subs hex 24 32)) 0x85a308d3))]
    (if (and (zero? s0hi) (zero? s0lo) (zero? s1hi) (zero? s1lo))
      #js [0 1 0 2 0 0]
      #js [s0hi s0lo s1hi s1lo 0 0])))

;; ----------------------------------------------------------------------------
;; Public PRNG.

(declare make)

(defrecord Xoroshiro128Js [st seed]
  opt-prng-stateful/PRNG
    (duplicate [_] (make seed))
    (jump [this] (jump! st) this)
    (uuid-seed [_] seed)
    (reset [this]
      (let [s (uuid->lane-state seed)]
        (dotimes [i 6] (aset st i (aget s i)))
        this))
    (rnd-int [_ a b] (next! st) (+ a (mod (unsigned-bit-shift-right (aget st 4) 1) (- b a))))
    (rnd-double [_ a b] (next! st) (+ a (* (raw->unit-double (aget st 4) (aget st 5)) (- b a)))))

(defn make
  "Create a thread-unsafe, JS-optimized xoroshiro128+ `PRNG` seeded with `uuid-seed`."
  [uuid-seed]
  (->Xoroshiro128Js (uuid->lane-state uuid-seed) uuid-seed))
