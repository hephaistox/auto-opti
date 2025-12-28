(ns auto-opti.eval
  "Optimisation evalutions. This namespace contains some simple evalutions for test and demo purposes.

  It contains also a `registry` mechanism that users should enrich."
  (:require
   [auto-opti                 :as-alias opti]
   [auto-opti.eval.montecarlo :as opt-montecarlo]))

(def registry
  {:montecarlo-pi #::opti{:doc "Returns an evaluation of pi thanks to the montercarlo method."
                          :rep :seed
                          :valid-pars opt-montecarlo/valid-pars
                          :eval opt-montecarlo/eval}})
