(ns auto-opti.prng.impl.well-macros)

(defmacro bit-shift-right-ns
  "A bit shift that doesn't do sign extension."
  [a b]
  `(let [n# ~b]
     (if (neg? n#)
       (bit-shift-left ~a (- n#))
       (bit-and (bit-shift-right Integer/MAX_VALUE (dec n#)) (bit-shift-right ~a n#)))))

(defmacro unsign
  "Convert a result based on a signed integer, and convert it to what it would have been for an unsigned integer."
  [x]
  `(let [v# ~x] (if (neg? v#) (+ 1 v#) v#)))

(defmacro limit-bits [x] `(bit-and int-max ~x))

(defmacro mat0-pos [t v] `(let [v# ~v] (bit-xor v# (bit-shift-right v# ~t))))

(defmacro mat0-neg
  [t v]
  `(let [v# ~v] (long (bit-xor v# (limit-bits (bit-shift-left v# (- ~t))))))
  2)

(defmacro add-mod-32 [a b] `(long (bit-and (+ ~a ~b) 0x01f)))
