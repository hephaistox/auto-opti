(ns auto-opti.tb.additive
  "All values measured at the same date are added.

  It is typically useful for a production entry, stock entry.

  So
  * No measured value returns `0`.
  * All measures are spot."
  (:require
   [auto-opti.maths       :as opt-maths]
   [auto-opti.tb.protocol :as opt-tb]))

(defrecord Additive [deltas]
  opt-tb/TbVar
    (measure [_this date data] (Additive. (update deltas date #(if (nil? %) data (+ data %)))))
    (get-measure [_this date] (get deltas date 0))
    (get-measures [_this] (vals deltas))
    (clamp [_this new-start new-end]
      (let [clamped-deltas (->> deltas
                                (keep (fn [[k v]]
                                        (when-not (and (< new-start k) (> k new-end)) [k v])))
                                (into (sorted-map)))]
        (Additive. clamped-deltas)))
    (mean [_this start end]
      (when (and (some? end) (some? start) (not= start end))
        (double (/ (apply + (vals deltas)) (- end start)))))
    (stddev [_this]
      (let [{:keys [s k]} (reduce (fn [{:keys [m s k]} v]
                                    (let [v-m (- v m)
                                          new-m (+ m (/ v-m k))]
                                      {:m new-m
                                       :s (+ s (* v-m (- v new-m)))
                                       :k (inc k)}))
                                  {:m 0.0
                                   :s 0.0
                                   :k 1}
                                  (vals deltas))]
        (if (> k 2) (opt-maths/sqrt (/ s (- k 2))) 0.0))))

(defn make [] (Additive. (sorted-map)))
