(ns auto-opti.time-based.impl.storage-strategy.deltas
  "Stores data in a collection of time / data pair fashion."
  (:require
   [auto-core.data.map                         :as utils-map]
   [auto-opti.time-based.impl.storage-strategy :as opt-tb-ss]))

(defrecord DeltaStrategy [deltas]
  opt-tb-ss/BucketData
    (assoc-date [_ k v] (DeltaStrategy. (assoc deltas k v)))
    (capacity [_] (count deltas))
    (get-after [this k]
      (let [k-after (utils-map/get-key-or-after deltas k)] (opt-tb-ss/get-exact this k-after)))
    (get-before [this k]
      (let [k-before (utils-map/get-key-or-before deltas k)] (opt-tb-ss/get-exact this k-before)))
    (get-exact [_ k] (get deltas k))
    (get-measures [this buckets] (mapv (fn [k] (opt-tb-ss/get-exact this k)) buckets))
    (nb-set [_] (count deltas))
    (range-dates [_] (let [v (keys deltas)] [(apply min v) (apply max v)]))
    (update-date [_ k f args] (DeltaStrategy. (apply update deltas k f args))))

(defn make [] (->DeltaStrategy (sorted-map)))
