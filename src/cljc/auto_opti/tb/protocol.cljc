(ns auto-opti.tb.protocol (:refer-clojure :exclude [min max inc dec +]))

(defprotocol TbVar
  (measure [this date data]
   "Add a measure `data` at `date`.")
  (get-measure [this date]
   "Data stored at `date`.")
  (clamp [this start end]
   "Clamp values between `start` and `end`")
  (cumulative [this]
   "Cumulates the value")
  (moving-average [this]
   "Moving average")
  (scalar-fn [this s-fn]
   "Apply `s-fn` to all values")
  (mean [this start end]
   "Mean value")
  (stddev [this]
   "Standard deviation")
  (get-measures [this]
   "All values as measured"))

