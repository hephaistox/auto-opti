;; Runnable example for `auto-opti.proba-dist`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/proba_dist.clj\")"
;; or via the whole suite: bb examples
;;
;; A `distribution` is built from a parameter map. `:dstb-name` selects the kind
;; (defaulting to :uniform) from the `distribution-registry`; the remaining keys
;; are the distribution's own parameters. A prng is created from `:seed` /
;; `:prng-name` unless an explicit `:prng` is supplied. `draw` samples a value.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.proba-dist :as dstb])

(println "=== auto-opti.proba-dist ===")

;; Uniform on [10;30[ with a fixed seed: the first draw is fully reproducible.
(let [d (dstb/distribution #::opti{:dstb-name :uniform
                                   :seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"
                                   :a 10
                                   :b 30})]
  (println "uniform [10;30[ draw :" (dstb/draw d)))

;; Normal distribution: location (mean) and scale (std-dev). Several draws.
(let [d (dstb/distribution #::opti{:dstb-name :normal
                                   :seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"
                                   :location 100
                                   :scale 5})]
  (println "normal 4 draws       :" (dstb/draws d 4)))

;; Categorical distribution: a weighted choice among keys.
(let [d (dstb/distribution #::opti{:dstb-name :categorical
                                   :seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"
                                   :category-probabilities {:red 0.5
                                                            :green 0.3
                                                            :blue 0.2}})]
  (println "categorical 6 draws  :" (dstb/draws d 6)))

;; Expected output (uniform draw is exact and asserted in the test-suite;
;; the normal / categorical lines are deterministic given the fixed seed):
;; === auto-opti.proba-dist ===
;; uniform [10;30[ draw : 27.96877525845019
;; normal 4 draws       : (... four doubles around 100 ...)
;; categorical 6 draws  : (... six of :red/:green/:blue ...)
