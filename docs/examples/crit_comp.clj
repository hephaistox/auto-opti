;; Runnable example for `auto-opti.crit-comp`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/crit_comp.clj\")"
;; or via the whole suite: bb examples
;;
;; A criteria comparator decides whether solution 1 is `:better`, `:worst` or
;; `:equal` to solution 2 given their criteria maps. `crit-comp-fn` looks a
;; strategy up in `default-registry` by `:crit-comp-name` and returns a ready
;; comparator function `(fn [crit1 crit2] ...)`.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.crit-comp :as crit-comp])

(println "=== auto-opti.crit-comp ===")

;; Two candidate solutions described by their criteria.
(def sol-a
  {:cost 12
   :delay 2
   :quality 100})
(def sol-b
  {:cost 13
   :delay 2
   :quality 100})

;; Hierarchical comparison: rank by :cost first, then :delay (smaller is better).
(def hierarchical
  (crit-comp/crit-comp-fn #::opti{:crit-comp-name :hierarchise
                                  :order [#::opti{:crit-comp-name :smaller
                                                  :crit-name :cost}
                                          #::opti{:crit-comp-name :smaller
                                                  :crit-name :delay}]}))

(println "hierarchise a vs b :" (hierarchical sol-a sol-b))

;; Weighted-sum comparison: combine criteria with weights, smaller sum is better.
(def weighted
  (crit-comp/crit-comp-fn #::opti{:crit-comp-name :weighted-sum
                                  :weights {:cost 10
                                            :delay 100}}))

(println "weighted a vs b    :"
         (weighted {:cost 12
                    :delay 2}
                   {:cost 13
                    :delay 4}))

;; Single-criterion helpers compare bare values directly.
(def smaller (crit-comp/crit-comp-fn #::opti{:crit-comp-name :smaller}))
(println "smaller 12 vs 20   :" (smaller 12 20))

;; Expected output:
;; === auto-opti.crit-comp ===
;; hierarchise a vs b : :better
;; weighted a vs b    : :better
;; smaller 12 vs 20   : :better
