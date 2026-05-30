;; Runnable example for `auto-opti.tb-var`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/tb_var.clj\")"
;; or via the whole suite: bb examples
;;
;; A `tb-var` (time-based variable) stores a value at a `bucket` (a natural
;; number representing simulation time). Two semantics ship:
;;   * additive  - `measure` adds to whatever is already at that bucket
;;                 (e.g. production recorded over time).
;;   * latest    - `measure` sets the value from that bucket onward until the
;;                 next measure (e.g. a stock level).
;; Each comes in a `deltas` (sparse) and `contiguous` (dense) storage form.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.tb-var :as tb-var])

(println "=== auto-opti.tb-var ===")

;; Additive: two measures on bucket 10 accumulate (3 + 2), bucket 11 gets 4.
(def production
  (-> (tb-var/tb-var-additive-deltas)
      (tb-var/measure 10 3)
      (tb-var/measure 10 2)
      (tb-var/measure 11 4)))

(println "additive @10       :" (tb-var/get-measure production 10))
(println "additive 8..13     :" (tb-var/get-measures production (range 8 14)))

;; Latest: a measure at bucket 10 holds until a new one; default before any.
(def stock
  (-> (tb-var/tb-var-latest-deltas :empty)
      (tb-var/measure 10 3)
      (tb-var/measure 11 4)))

(println "latest 8..13       :" (tb-var/get-measures stock (range 8 14)))

;; Aggregator: group buckets [10;20[ into steps of 5, then store on aggregates.
(def aggregated
  (-> [#::opti{:start-bucket 10
               :end-bucket 20
               :step 5}]
      tb-var/aggregator
      (tb-var/tb-var-aggregated (tb-var/tb-var-additive-deltas))
      (tb-var/measure 10 3)
      (tb-var/measure 14 4)))

(println "aggregated 9..16   :" (mapv #(tb-var/get-measure aggregated %) [9 10 11 14 15 16]))

;; Expected output:
;; === auto-opti.tb-var ===
;; additive @10       : 5
;; additive 8..13     : [0 0 5 4 0 0]
;; latest 8..13       : [:empty :empty 3 4 4 4]
;; aggregated 9..16   : [nil 7 7 7 0 0]
