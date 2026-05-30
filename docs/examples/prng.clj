;; Runnable example for `auto-opti.prng`.
;;
;; Run with: clojure -M -e "(load-file \"docs/examples/prng.clj\")"
;; or via the whole suite: bb examples
;;
;; A `prng` is a stateful pseudo-random number generator built from a parameter
;; map. The map selects an implementation by name (defaulting to :xoroshiro128)
;; and a seed; the same seed always yields the same stream, which is exactly what
;; reproducible simulation needs.
;;
;; SECURITY NOTE: these generators are NOT cryptographically secure. Never use
;; them for keys, tokens, salts, or any security-sensitive randomness.

(require '[auto-opti :as-alias opti])
(require '[auto-opti.prng :as prng])

(println "=== auto-opti.prng ===")

;; Build a prng from a parameter map. With no seed, a fixed default seed is used,
;; so this stream is deterministic across runs. Each draw advances the state.
(let [gen (prng/prng {})]
  ;; Draw three integers, each in its own range.
  (println "three as-int draws:"
           [(prng/as-int gen 0 10) (prng/as-int gen 6 12) (prng/as-int gen 60 72)]))

;; A fresh default prng restarts the same deterministic stream.
(let [gen (prng/prng {})] (println "first as-double   :" (prng/as-double gen 0.0 10.0)))

;; An explicit seed reproduces an exact value across independent generators.
(let [seeded (prng/prng #::opti{:prng-name :xoroshiro128
                                :seed #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3d"})]
  (println "seeded as-int     :" (prng/as-int seeded 0 10)))

;; Expected output:
;; === auto-opti.prng ===
;; three as-int draws: [6 7 67]
;; first as-double   : 0.2594879491552782
;; seeded as-int     : 8
