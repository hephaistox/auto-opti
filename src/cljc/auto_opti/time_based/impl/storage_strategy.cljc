(ns auto-opti.time-based.impl.storage-strategy
  "Strategies to store data in `tb-var`.

  Note: `get-exact` and `get-measures` are redundant functionaly but are here to optimize performance.")

(defprotocol BucketData
  (assoc-date [this bucket v]
   "Associates value `v` to date `bucket`")
  (capacity [_]
   "Returns the size of the capacity.")
  (get-after [this bucket]
   "Returns the first value after `bucket`")
  (get-before [this bucket]
   "Returns the first value before `bucket`")
  (get-exact [this bucket]
   "Get the value at `bucket`")
  (get-measures [this buckets]
   "A subset of values between `[bucket-start;bucket-end[`.")
  (nb-set [_]
   "Returns the number of data assoced in the datastructure")
  (range-dates [_]
   "Returns the range of the dataset")
  (update-date [this bucket f args]
   "Updates the value at `bucket` with function `f`,  `args` is a collection of supplementary data passed to `f`"))

(defn occupation-rate
  "Calculates the occupation rate of `storage`."
  [storage]
  (/ (nb-set storage) (capacity storage)))
