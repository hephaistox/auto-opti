;; End-to-end runnable example assembling several `auto-opti` features.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/end_to_end.clj\")"
;; or via the whole suite: bb examples
;;
;; auto-opti deliberately ships building blocks, not a fixed pipeline: assembling
;; them is the user's responsibility. This snippet shows one such assembly -- a
;; tiny random-search optimizer:
;;
;;   prng + proba-dist  -> draw random candidate solutions
;;   a cost model       -> evaluate each candidate's criteria
;;   sample             -> summarize the batch of costs
;;   crit-comp          -> select the best candidate (hierarchical comparison)
;;
;; The problem: pick a (machine-speed, batch-size) pair minimizing a cost, where
;; cost trades off throughput against work-in-progress.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.prng :as prng])
(require '[auto-opti.proba-dist :as dstb])
(require '[auto-opti.sample :as sample])
(require '[auto-opti.crit-comp :as crit-comp])

(println "=== auto-opti end-to-end (random search) ===")

;; One shared, seeded prng drives every distribution -> fully reproducible run.
(def gen
  (prng/prng #::opti{:prng-name :xoroshiro128
                     :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"}))

;; Decision variables drawn from distributions sharing the prng.
(def speed-dstb
  (dstb/distribution #::opti{:dstb-name :uniform
                             :prng gen
                             :a 1.0
                             :b 5.0}))
(def batch-dstb
  (dstb/distribution #::opti{:dstb-name :uniform-int
                             :prng gen
                             :a 1
                             :b 20}))

;; Cost model turning a candidate into a criteria map (lower is better).
(defn evaluate
  [{:keys [speed batch]}]
  (let [throughput-cost (/ 10.0 speed) ; faster machine -> cheaper
        wip-cost (* 0.5 batch)]        ; bigger batch -> more work-in-progress
    {:speed speed
     :batch batch
     :cost (+ throughput-cost wip-cost)}))

;; Draw a batch of candidate solutions and evaluate each.
(def candidates
  (->> (repeatedly (fn []
                     {:speed (dstb/draw speed-dstb)
                      :batch (dstb/draw batch-dstb)}))
       (take 20)
       (map evaluate)))

;; Summarize the batch of costs with sample stats.
(def costs (map :cost candidates))
(println "candidates         :" (count candidates))
(println "mean cost          :" (sample/average costs))
(println "min/median cost    :" [(reduce min costs) (sample/median costs)])

;; Select the best candidate: smallest :cost wins (hierarchical, one criterion).
(def better?
  (crit-comp/crit-comp-fn #::opti{:crit-comp-name :hierarchise
                                  :order [#::opti{:crit-comp-name :smaller
                                                  :crit-name :cost}]}))

(def best
  (reduce (fn [acc c] (if (= :better (better? c acc)) c acc)) (first candidates) (rest candidates)))

(println "best candidate     :" (select-keys best [:speed :batch]))
(println "best cost          :" (:cost best))

;; Sanity check: the chosen solution really is the minimum-cost one.
(assert (= (:cost best) (reduce min costs)) "best must be the minimum-cost candidate")
(println "OK: best == min cost")

;; Expected output (deterministic given the fixed seed):
;; === auto-opti end-to-end (random search) ===
;; candidates         : 20
;; mean cost          : (... a double ...)
;; min/median cost    : [(... min ...) (... median ...)]
;; best candidate     : {:speed (... ), :batch (... )}
;; best cost          : (... the minimum cost ...)
;; OK: best == min cost
