(ns auto-opti.prng.impl.xoroshiro256-jvm
  "Xoshiro256+ PRNG implementation optimized for Clojure/JVM.

  Uses primitive longs and a mutable `long-array` of four words for maximum
  performance on the JVM. Larger state (period 2^256 - 1) than
  `xoroshiro128-jvm`. Exposed through the registry as `:xoroshiro256-jvm`.

  WARNING - not thread-safe. The state is mutated in place; call `duplicate` to
  get an independent generator per thread.

  Algorithm reference: https://prng.di.unimi.it/xoshiro256plus.c"
  (:import [java.util UUID])
  (:require
   [auto-opti.prng.impl.xoro-common :as xoro-common]
   [auto-opti.prng.stateful         :as opt-prng-stateful]))

(defn uuid->state
  "Convert UUID to a 256-bit state [s0 s1 s2 s3]."
  ^longs [^UUID uuid]
  (let [msb ^long (.getMostSignificantBits uuid)
        lsb ^long (.getLeastSignificantBits uuid)
        ;; SplitMix64 finalizer, chained to fan a 128-bit seed into 256 bits
        s0 (xoro-common/mix msb)
        s1 (xoro-common/mix s0)
        s2 (xoro-common/mix lsb)
        s3 (xoro-common/mix s2)]
    (long-array (if (and (zero? s0) (zero? s1)) [1 0 0 0] [s0 s1 s2 s3]))))

(defn xoroshiro-next!
  "Generate next value and update state.

  Xoshiro256+ algorithm (parameters: a=17, b=45). Mutates state array."
  ^long [^longs state]
  (let [s0 (aget state 0)
        s1 (aget state 1)
        s2 (aget state 2)
        s3 (aget state 3)
        result (unchecked-add s0 s3) ; result = s[0] + s[3]
        t (bit-shift-left s1 17) ; t = s[1] << 17
        s2 (bit-xor s2 s0) ; s[2] ^= s[0]
        s3 (bit-xor s3 s1) ; s[3] ^= s[1]
        s1 (bit-xor s1 s2) ; s[1] ^= s[2]
        s0 (bit-xor s0 s3) ; s[0] ^= s[3]
        s2 (bit-xor s2 t) ; s[2] ^= t
        s3 (xoro-common/rotl s3 45)] ; s[3] = rotl(s[3], 45)
    (aset state 0 s0)
    (aset state 1 s1)
    (aset state 2 s2)
    (aset state 3 s3)
    result))

(def ^:private ^long jump-0 (java.lang.Long/parseUnsignedLong "180ec6d33cfd0aba" 16))
(def ^:private ^long jump-1 (java.lang.Long/parseUnsignedLong "d5a61266f0c9392c" 16))
(def ^:private ^long jump-2 (java.lang.Long/parseUnsignedLong "a9582618e03fc9aa" 16))
(def ^:private ^long jump-3 (java.lang.Long/parseUnsignedLong "39abdc4529b1661c" 16))

(defn jump!
  "Advance `state` by 2^128 steps - equivalent to 2^128 calls to
   `xoroshiro-next!`. Mutates and returns `state`."
  ^longs [^longs state]
  (let [jump (long-array [jump-0 jump-1 jump-2 jump-3])]
    (loop [i 0
           a0 0
           a1 0
           a2 0
           a3 0]
      (if (< i 4)
        (let [ji (aget jump i)
              [a0 a1 a2 a3] (loop [b 0
                                   a0 a0
                                   a1 a1
                                   a2 a2
                                   a3 a3]
                              (if (< b 64)
                                (let [[a0 a1 a2 a3] (if (zero? (bit-and ji (bit-shift-left 1 b)))
                                                      [a0 a1 a2 a3]
                                                      [(bit-xor a0 (aget state 0))
                                                       (bit-xor a1 (aget state 1))
                                                       (bit-xor a2 (aget state 2))
                                                       (bit-xor a3 (aget state 3))])]
                                  (xoroshiro-next! state)
                                  (recur (inc b) a0 a1 a2 a3))
                                [a0 a1 a2 a3]))]
          (recur (inc i) a0 a1 a2 a3))
        (do (aset state 0 (long a0))
            (aset state 1 (long a1))
            (aset state 2 (long a2))
            (aset state 3 (long a3))
            state)))))

(declare make)

(defrecord Xoroshiro256Jvm [^longs state ^UUID seed]
  opt-prng-stateful/PRNG
    (duplicate [_] (make seed))
    (jump [this] (jump! state) this)
    (uuid-seed [_] seed)
    (reset [this]
      (let [s (uuid->state seed)]
        (dotimes [i 4] (aset state i (aget s i)))
        this))
    (rnd-int [_ a b] (xoro-common/raw->int (xoroshiro-next! state) a (- (long b) (long a))))
    (rnd-double [_ a b]
      (xoro-common/raw->double (xoroshiro-next! state) (double a) (- (double b) (double a)))))

(defn make
  "Create a thread-unsafe, JVM-optimized xoshiro256+ `PRNG` seeded with `uuid-seed`."
  [^UUID uuid-seed]
  (->Xoroshiro256Jvm (uuid->state uuid-seed) uuid-seed))

(defn make-generators
  "Create the raw generator map used for benchmarking and the map-based quality
  suite. Prefer `make` for production use (it returns a `PRNG`)."
  [^UUID uuid-seed]
  (let [state (uuid->state uuid-seed)]
    {:state state
     :seed uuid-seed
     :meta {:period 256
            :platform :clojure}
     :next-double (fn [a b] (xoro-common/next-double-fn xoroshiro-next! state a b))
     :next-raw (fn [] (xoro-common/next-raw-fn xoroshiro-next! state))
     :next-int (fn [a b] (xoro-common/next-int-fn xoroshiro-next! state a b))
     :next-intu (fn [a b]
                  (xoro-common/next-int-fn-uniformity-efficient xoroshiro-next! state a b))}))
