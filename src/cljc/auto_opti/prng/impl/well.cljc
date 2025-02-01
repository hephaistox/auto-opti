(ns auto-opti.prng.impl.well
  "Adapted from `https://github.com/hugoduncan/criterium/blob/develop/src/criterium/well.clj`."
  #?(:cljs (:require-macros
            [auto-opti.prng.impl.well-macros :refer [add-mod-32 mat0-neg mat0-pos unsign]])
     :clj (:require
           [auto-opti.prng.impl.well-macros :refer [add-mod-32 mat0-neg mat0-pos unsign]]))
  (:require
   [auto-opti.maths :as opt-maths]))

(def int-max (bit-or (bit-shift-left opt-maths/infinity-integer 1) 1))

(defn well-rng-1024a
  "Well RNG 1024a.

  See: Improved Long-Period Generators Based on Linear Recurrences Modulo 2
  F. Panneton, P. L'Ecuyer and M. Matsumoto
  http://www.iro.umontreal.ca/~panneton/WELLRNG.html"
  ([]
   (well-rng-1024a (long-array 32 (repeatedly 32 #(rand-int opt-maths/infinity-integer)))
                   (rand-int 32)))
  ([^longs state ^long index]
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
     (lazy-seq (cons (unsign (* (aget state new-index) fact)) (well-rng-1024a state new-index))))))
