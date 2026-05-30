(ns auto-opti.prng.impl.xoroshiro128-jvm
  "Xoroshiro128+ PRNG implementation optimized for Clojure/JVM.

  Uses primitive longs and a mutable `long-array` state for maximum performance
  on the JVM. Exposed through the registry as `:xoroshiro128-jvm`.

  WARNING - not thread-safe. The state is mutated in place, so a single
  generator must not be shared across threads; call `duplicate` to get an
  independent generator per thread.

  This implementation matches the canonical C reference
  (https://prng.di.unimi.it/xoroshiro128plus.c) bit for bit, see
  `reference-implementation-test`."
  (:import [java.util UUID])
  (:require
   [auto-opti.prng.impl.xoro-common :as xoro-common]
   [auto-opti.prng.stateful         :as opt-prng-stateful]))

(defn uuid->state
  "Convert UUID to 128-bit state [s0 s1] for xoroshiro128+"
  ^longs [^UUID uuid]
  (let [msb ^long (.getMostSignificantBits uuid)
        lsb ^long (.getLeastSignificantBits uuid)
        ;; SplitMix64 finalizer
        s0 (xoro-common/mix msb)
        s1 (xoro-common/mix lsb)]
    ;; xoroshiro requires non-zero state
    (long-array (if (and (zero? s0) (zero? s1)) [1 0] [s0 s1]))))

(defn xoroshiro-next!
  "Generate next value and update state.
   Xoroshiro128+ algorithm (2018 version with parameters a=24, b=16, c=37).
   Mutates state array."
  ^long [^longs state]
  (let [s0 (aget state 0)
        s1 (aget state 1)
        result (unchecked-add s0 s1)
        s1 (bit-xor s1 s0) ; s1 ^= s0
        new-s0 (bit-xor (bit-xor (xoro-common/rotl s0 24) s1) (bit-shift-left s1 16)) ; rotl(s0,24) ^ s1 ^ (s1<<16)
        new-s1 (xoro-common/rotl s1 37)] ; rotl(s1, 37)
    (aset state 0 new-s0)
    (aset state 1 new-s1)
    result))

(def ^:private ^long jump-0 (java.lang.Long/parseUnsignedLong "df900294d8f554a5" 16))
(def ^:private ^long jump-1 (java.lang.Long/parseUnsignedLong "170865df4b3201fc" 16))

(defn jump!
  "Advance `state` by 2^64 steps - equivalent to 2^64 calls to `xoroshiro-next!`.
   Mutates and returns `state`."
  ^longs [^longs state]
  (let [jump (long-array [jump-0 jump-1])]
    (loop [i 0
           a0 0
           a1 0]
      (if (< i 2)
        (let [ji (aget jump i)
              [a0 a1] (loop [b 0
                             a0 a0
                             a1 a1]
                        (if (< b 64)
                          (let [[a0 a1] (if (zero? (bit-and ji (bit-shift-left 1 b)))
                                          [a0 a1]
                                          [(bit-xor a0 (aget state 0))
                                           (bit-xor a1 (aget state 1))])]
                            (xoroshiro-next! state)
                            (recur (inc b) a0 a1))
                          [a0 a1]))]
          (recur (inc i) a0 a1))
        (do (aset state 0 (long a0)) (aset state 1 (long a1)) state)))))

(declare make)

(defrecord Xoroshiro128Jvm [^longs state ^UUID seed]
  opt-prng-stateful/PRNG
    (duplicate [_] (make seed))
    (jump [this] (jump! state) this)
    (uuid-seed [_] seed)
    (reset [this]
      (let [s (uuid->state seed)]
        (aset state 0 (aget s 0))
        (aset state 1 (aget s 1))
        this))
    (rnd-int [_ a b] (xoro-common/raw->int (xoroshiro-next! state) a (- (long b) (long a))))
    (rnd-double [_ a b]
      (xoro-common/raw->double (xoroshiro-next! state) (double a) (- (double b) (double a)))))

(defn make
  "Create a thread-unsafe, JVM-optimized xoroshiro128+ `PRNG` seeded with `uuid-seed`."
  [^UUID uuid-seed]
  (->Xoroshiro128Jvm (uuid->state uuid-seed) uuid-seed))

(defn make-generators
  "Create the raw generator map used for benchmarking and the map-based quality
  suite. Returns a map of range generators closing over a shared mutable state.

  Prefer `make` for production use (it returns a `PRNG`)."
  [^UUID uuid-seed]
  (let [state (uuid->state uuid-seed)]
    {:state state
     :seed uuid-seed
     :meta {:state-bits 128
            :platform :clojure}
     :next-double (fn [a b] (xoro-common/next-double-fn xoroshiro-next! state a b))
     :next-raw (fn [] (xoro-common/next-raw-fn xoroshiro-next! state))
     :next-int (fn [a b] (xoro-common/next-int-fn xoroshiro-next! state a b))
     :next-intu (fn [a b]
                  (xoro-common/next-int-fn-uniformity-efficient xoroshiro-next! state a b))}))
