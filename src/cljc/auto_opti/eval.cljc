(ns auto-opti.eval
  "Optimisation evalutions. This namespace contains some simple evalution for test purposed and is expected to be enriched by user or other libraries."
  (:require
   [auto-opti                 :as-alias opti]
   [auto-opti.eval.montecarlo :as opt-montecarlo]))

(def registry
  {:montecarlo-pi #::opti{:doc "Returns an evaluation of pi thanks to the montercarlo method."
                          :rep :seed
                          :valid-pars opt-montecarlo/valid-pars
                          :eval opt-montecarlo/eval}})
