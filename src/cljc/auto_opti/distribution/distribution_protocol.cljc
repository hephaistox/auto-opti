(ns auto-opti.distribution.distribution-protocol "Probabilistic distributions protocol.")

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
  [distribution]
  (- (quantile distribution 0.75) (quantile distribution 0.25)))

(defn summary
  "Returns the 5-number distribution summary and the interquartile range."
  [distribution]
  (let [q1 (quantile distribution 0.25)
        q3 (quantile distribution 0.75)]
    {:min (minimum distribution)
     :q1 q1
     :median (quantile distribution 0.5)
     :q3 q3
     :max (maximum distribution)
     :iqr (when (and q1 q3) (- q3 q1))}))

(defn critical-value
  ([distribution] (critical-value distribution 0.05))
  ([distribution alpha] (critical-value distribution alpha :<>))
  ([distribution alpha tails]
   (case tails
     :<> (quantile distribution (- 1 (* 0.5 alpha)))
     :< (quantile distribution alpha)
     :> (quantile distribution (- 1 alpha)))))
