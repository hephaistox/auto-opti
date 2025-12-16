(ns auto-opti.prng.impl.well
  "Adapted from `https://github.com/hugoduncan/criterium/blob/develop/src/criterium/well.clj`."
  #?(:cljs (:require-macros
            [auto-opti.prng.impl.well-macros :refer [add-mod-32 mat0-neg mat0-pos unsign]])
     :clj (:require
           [auto-opti.prng.impl.well-macros :refer [add-mod-32 mat0-neg mat0-pos unsign]]))
  (:require
   [auto-opti.prng.stateful-wrapper :as opt-stateful-wrapper]
   [auto-opti.prng.stateless        :as opt-prng-stateless]
   [cljc-long.core])
  #?(:clj (:import [java.util UUID])))

(defn well-rng-1024a
  "Well RNG 1024a.
  See: Improved Long-Period Generators Based on Linear Recurrences Modulo 2
  F. Panneton, P. L'Ecuyer and M. Matsumoto
  http://www.iro.umontreal.ca/~panneton/WELLRNG.html"
  [^longs state ^long index]
  {:pre [(<= 0 index 32)]}
  (let [m1 3
        m2 24
        m3 10
        fact 2.32830643653869628906e-10
        new-index (add-mod-32 index 31)
        z0 (aget state new-index)
        z1 (bit-xor (aget state index) (mat0-pos 8 (aget state (add-mod-32 index m1))))
        z2 (bit-xor (mat0-neg -19 (aget state (add-mod-32 index m2)))
                    (mat0-neg -14 (aget state (add-mod-32 index m3))))]
    (aset state index (bit-xor z1 z2))
    (aset state new-index (bit-xor (bit-xor (mat0-neg -11 z0) (mat0-neg -7 z1)) (mat0-neg -13 z2)))
    (lazy-seq (cons (unsign (* (aget state new-index) fact)) (well-rng-1024a state new-index)))))

;; ********************************************************************************
;; Create prng

(defrecord Well [state index uuid-seed]
  opt-prng-stateless/PRNGStateless
    (jump [this] this)
    (uuid-seed [_] uuid-seed)
    (next [this] (->Well (rest state)))
    (peek-int [_ min-int max-int]
      (let [r (- max-int min-int)]
        (-> (+ min-int (* r (first state)))
            long)))
    (peek-double [_ a b] (let [r (- a b)] (+ b (* r (first state))))))

(defn- uuid->state
  "Convert a UUID to a 32-element long array for WELL RNG state.
  Uses the UUID bits as seed for a simple LCG to generate all 32 values."
  [uuid]
  (let [;; Get the two 64-bit parts of the UUID
        #?@(:cljs [hex (str uuid)
                   cleaned (.replace hex "-" "")]
            :clj [])
        msb #?(:clj (.getMostSignificantBits uuid)
               :cljs (js/parseInt (.substring cleaned 0 16) 16))
        lsb #?(:clj (.getLeastSignificantBits uuid)
               :cljs (js/parseInt (.substring cleaned 16 32) 16))]
    ;; Expand UUID bits into 32 state values using a mixing function
    ;; Generate positive integers like the original (rand-int opt-maths/infinity-integer)
    (loop [idx 0
           result (long-array 32)]
      (if (< idx 32)
        (let [;; Mix the UUID bits with the index to get variation
              ;; Use different parts of msb and lsb for each index
              seed (bit-xor msb lsb (unchecked-multiply idx 0x9e3779b9))
              ;; Apply mixing rounds (MurmurHash3 finalizer)
              h1 (bit-xor seed (unsigned-bit-shift-right seed 16))
              h2 (unchecked-multiply h1 0x85ebca6b)
              h3 (bit-xor h2 (unsigned-bit-shift-right h2 13))
              h4 (unchecked-multiply h3 0xc2b2ae35)
              h5 (bit-xor h4 (unsigned-bit-shift-right h4 16))
              ;; Match original behavior: positive integers [0, Integer/MAX_VALUE)
              final-val (mod (Math/abs (long h5)) 2147483647)]
          (aset result idx final-val)
          (recur (inc idx) result))
        result))))

(defn make-stateless
  "With the optional `uuid` parameter used as a seed, the stateless version of the xoroshiro prng is generated."
  [uuid-seed]
  (if #?(:clj (instance? UUID uuid-seed)
         :cljs true)
    ;; UUID provided - initialize state from it
    ;; Use a deterministic starting index based on the UUID
    (let [state (uuid->state uuid-seed)
          start-idx (mod (aget state 0) 32)]
      (->Well (well-rng-1024a state start-idx) 0 uuid-seed))
    ;; State array provided - use existing behavior
    (make-stateless (rand-int 32))))

(defn make
  "With the optional `uuid` parameter used as a seed, the stateful version of the xoroshiro prng is generated."
  ([uuid-seed] (opt-stateful-wrapper/make (make-stateless uuid-seed)))
  ([] (opt-stateful-wrapper/make (make-stateless (random-uuid)))))
