(ns auto-opti.eval
  (:require
   [auto-opti.eval.montecarlo :as opt-montecarlo]))

(def registry
  {:montecarlo-pi {:doc "Returns an evaluation of pi thanks to the montercarlo method."
                   :rep :seed
                   :build-eval opt-montecarlo/build-valid
                   :f opt-montecarlo/eval}})
