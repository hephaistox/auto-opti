;; Runnable example for `auto-opti.maths` (+ `maths.gamma`, `maths.weighted-sum`).
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/maths.clj\")"
;; or via the whole suite: bb examples
;;
;; `auto-opti.maths` is a collection of cross-platform math helpers that return
;; the same results on the JVM and in JavaScript. `maths.gamma` adds the gamma /
;; beta family, and `maths.weighted-sum` a small weighted-sum over a criteria map.

(require '[auto-opti.maths :as maths])
(require '[auto-opti.maths.gamma :as gamma])
(require '[auto-opti.maths.weighted-sum :as weighted-sum])

(println "=== auto-opti.maths ===")

(println "square 4           :" (maths/square 4))
(println "sqrt 16            :" (maths/sqrt 16))
(println "pow 3 3            :" (maths/pow 3.0 3.0))
(println "clamp 1..13 of 15  :" (maths/clamp 1 13.0 15))
;; polynomial 3x^2 + 2x + 1 at x=2 -> 17
(println "polynomial @2      :" (maths/polynomial-value 2 [3 2 1]))
;; proportion of even numbers in the collection
(println "proportion even    :" (maths/proportion even? [10 2 10 2 20 1 50 22 54 90]))

;; gamma(5) = 4! = 24
(println "gamma 5            :" (gamma/gamma 5))

;; weighted-sum: 10*cost + 100*delay over the criteria map
(println "weighted-sum       :"
         (weighted-sum/weighted-sum {:cost 10
                                     :delay 100}
                                    {:cost 12
                                     :delay 2}))

;; Expected output:
;; === auto-opti.maths ===
;; square 4           : 16
;; sqrt 16            : 4.0
;; pow 3 3            : 27.0
;; clamp 1..13 of 15  : 13.0
;; polynomial @2      : 17
;; proportion even    : 0.9
;; gamma 5            : 24.0
;; weighted-sum       : 320
