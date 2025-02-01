(ns auto-opti.time-based.protocol
  "Time-based measures.
  This namespace could be used to creates new `time-based` implementation.")

(defprotocol TimeBased
  (default [this]
   "Default value before measurement.")
  (get-measure [this bucket]
   "Returns the data stored at time `bucket`.")
  (get-measures [this buckets]
   "Returns the measures for each element in `buckets`.")
  (measure [this bucket data]
   "Returns a new instance of TimeBased with `data` measured at time `bucket`. The effect will depend on the kind of TimeBased."))
