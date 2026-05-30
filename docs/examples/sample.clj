;; Runnable example for `auto-opti.sample`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/sample.clj\")"
;; or via the whole suite: bb examples
;;
;; `auto-opti.sample` provides descriptive statistics over plain collections of
;; numbers: average, variance, standard deviation, median, midrange, frequencies.
;; Useful to summarize the results of a batch of simulation draws.

(require '[auto-opti.sample :as sample])

(println "=== auto-opti.sample ===")

(def xs [2 4 4 4 5 5 7 9])

(println "data               :" xs)
(println "n                  :" (sample/n xs))
(println "average            :" (sample/average xs))
(println "variance           :" (sample/variance xs))
(println "standard-deviation :" (sample/standard-deviation xs))
(println "median (odd count) :" (sample/median [1 10 14 17 2000]))
(println "median (even count):" (sample/median [1 10 20 1000]))
(println "midrange           :" (sample/midrange [2 98 13 10]))

;; Expected output:
;; === auto-opti.sample ===
;; data               : [2 4 4 4 5 5 7 9]
;; n                  : 8
;; average            : 5
;; variance           : 4.0
;; standard-deviation : 2.0
;; median (odd count) : 14
;; median (even count): 15
;; midrange           : 50
