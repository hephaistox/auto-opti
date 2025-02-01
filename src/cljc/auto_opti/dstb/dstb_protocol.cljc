(ns auto-opti.dstb.dstb-protocol "Probabilistic distributions protocol.")

(defprotocol Distribution
  (draw [this]
   "Returns a random value following that distribution.")
  (median [this]
   "Returns the median of the distribution.")
  (cumulative [this p]
   "Returns the cumulative probability before `p`.")
  (minimum [this]
   "Minimum.")
  (maximum [this]
   "Maximum.")
  (quantile [this x]
   "Returns the quantile of the distribution before `x`."))

(defn iqr
  "Returns the interquartile range of that `distribution`."
  [dstb]
  (- (quantile dstb 0.75) (quantile dstb 0.25)))

(defn summary
  "Returns the 5-number distribution summary and the interquartile range."
  [dstb]
  (let [q1 (quantile dstb 0.25)
        q3 (quantile dstb 0.75)]
    {:min (minimum dstb)
     :q1 q1
     :median (quantile dstb 0.5)
     :q3 q3
     :max (maximum dstb)
     :iqr (when (and q1 q3) (- q3 q1))}))

(defn critical-value
  ([dstb] (critical-value dstb 0.05))
  ([dstb alpha] (critical-value dstb alpha :<>))
  ([dstb alpha tails]
   (case tails
     :<> (quantile dstb (- 1 (* 0.5 alpha)))
     :< (quantile dstb alpha)
     :> (quantile dstb (- 1 alpha)))))
