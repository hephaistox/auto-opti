(ns auto-opti.maths.weighted-sum {:no-doc true})

(defn weighted-sum
  "Calculate a weighted sum, `weights` is a map associating a `weight` to a keyword. This keyword should appear in `crits`."
  [weights crits]
  (reduce (fn [s [crit-name weight]] (when weight (+ s (* weight (get crits crit-name 0)))))
          0
          weights))
