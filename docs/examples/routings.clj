;; Runnable example for `auto-opti.routings`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/routings.clj\")"
;; or via the whole suite: bb examples
;;
;; `auto-opti.routings` models jobshop data: a map of `route-id` -> ordered list
;; of operations, each operation being a machine `:m` and a processing time `:pt`
;; (an integer or a distribution parameter map). `start` prepares the model
;; against a prng (turning processing times into live distributions and building
;; a categorical distribution over routes), then `pick-route-id` samples a route.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.prng :as prng])
(require '[auto-opti.routings :as routings])

(println "=== auto-opti.routings ===")

;; A two-route jobshop model. Processing times are distribution parameter maps.
(def model
  #::opti{:routes {:blue {:probability 0.6
                          :operations [{:m :m4
                                        :pt #::opti{:dstb-name :uniform
                                                    :a 10
                                                    :b 15}}
                                       {:m :m2
                                        :pt 3}
                                       {:m :m1
                                        :pt #::opti{:dstb-name :normal
                                                    :location 8
                                                    :scale 1}}]}
                   :purple {:probability 0.4
                            :operations [{:m :m4
                                          :pt 5}
                                         {:m :m3
                                          :pt #::opti{:dstb-name :uniform
                                                      :a 4
                                                      :b 9}}
                                         {:m :m1
                                          :pt 2}]}}})

;; The distinct, sorted machine names used across all routes.
(println "machines           :" (routings/machines model))

;; Prepare the model: bind a prng and materialize the distributions.
(def prng
  (prng/prng #::opti{:prng-name :xoroshiro128
                     :seed #uuid "e85427c1-ed25-4ed4-9b11-52238d268265"}))
(def started (routings/start model prng))

;; Sample a few route-ids weighted by each route's :probability.
(println "picked route-ids   :" (repeatedly 5 #(routings/pick-route-id started)))

;; Expected output:
;; === auto-opti.routings ===
;; machines           : (:m1 :m2 :m3 :m4)
;; picked route-ids   : (... five of :blue / :purple, weighted 0.6 / 0.4 ...)
