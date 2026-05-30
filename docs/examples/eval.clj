;; Runnable example for `auto-opti.eval`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/eval.clj\")"
;; or via the whole suite: bb examples
;;
;; `auto-opti.eval` exposes a `registry` of evaluations. Each entry is a map with
;; auto-opti-namespaced keys: a `::opti/valid-pars` validator and an `::opti/eval`
;; function. An evaluation turns a model + a representation (`::opti/rep`) into a
;; numeric solution. The built-in `:montecarlo-pi` evaluation approximates pi by
;; sampling points in a square and counting those inside the inscribed circle.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.eval :as eval])

(println "=== auto-opti.eval ===")

;; Look up the montecarlo-pi evaluation in the registry. The registry entry is a
;; map with `auto-opti`-namespaced keys, so reach in with ::opti/eval etc.
(def montecarlo (get eval/registry :montecarlo-pi))
(def evaluate (::opti/eval montecarlo))
(def valid-pars (::opti/valid-pars montecarlo))

;; The model describes the problem; the rep carries the seed driving the draws.
(def model
  {:radius 100
   :iterations 100000})
(def rep {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"})

;; Validate the model first: nil means valid.
(println "valid-pars (ok)    :" (valid-pars {} nil model))
(println "valid-pars (bad)   :"
         (valid-pars {}
                     nil
                     {:radius "r"
                      :iterations "i"}))

;; Run the evaluation. Signature: [params iterator-fn model rep].
;; With 100000 iterations this lands very close to pi.
(println "pi approximation   :" (evaluate {} nil model rep))

;; Expected output:
;; === auto-opti.eval ===
;; valid-pars (ok)    : nil
;; valid-pars (bad)   : {:model {:error {:radius [...] :iterations [...]} ...}}
;; pi approximation   : 3.14... (within 0.01 of Math/PI)
