(ns auto-opti.maths.weighted-sum)

(defn weighted-sum
  "Calculate a weighted sum, `weights` is a map associating a `weight` to a keyword. This keyword should appear in `crits`."
  [weights crits]
  (reduce (fn [s [crit-name weight]] (+ s (* weight (get crits crit-name 0)))) 0 weights))
