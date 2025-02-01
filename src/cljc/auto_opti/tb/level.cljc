(ns auto-opti.tb.level
  "All values measured at the same date are added.

  It is typically useful for a level of stock

  So
  * No measured value returns `0`.
  * All measures are spot."
  (:require
   [auto-core.data.map    :as utils-map]
   [auto-opti.maths       :as opt-maths]
   [auto-opti.tb.protocol :as opt-tb]))

(defrecord Level [deltas]
  opt-tb/TbVar
    (measure [_this date data] (Level. (assoc deltas date data)))
    (get-measure [_this date]
      (let [k-before (utils-map/get-key-or-before deltas date)] (get deltas k-before 0)))
    (get-measures [_this] (vals deltas))
    (clamp [_this new-start new-end]
      (->> deltas
           (keep (fn [[k v]]
                   (when-not (and (< new-start k) (> k new-end))
                     [(min (max k new-start) new-end) v])))
           (into (sorted-map))
           Level.))
    (mean [_this start end]
      (when (and (some? end) (some? start) (not= start end))
        (double (/ (apply + (vals deltas)) (- end start)))))
    (cumulative [_this]
      (reduce (fn [{:keys [deltas cumulated]} [k v]]
                (let [cumulated (+ cumulated v)]
                  {:deltas (assoc deltas k cumulated)
                   :cumulated cumulated}))
              {:deltas deltas
               :cumulated 0}
              deltas))
    (scalar-fn [_this s-fn] (Level. (update-vals deltas s-fn)))
    (moving-average [_this]
      (Level. (:deltas (reduce (fn [{:keys [i deltas cumulated]} [k v]]
                                 (let [cumulated (+ cumulated v)]
                                   {:deltas (assoc deltas k (double (/ cumulated i)))
                                    :i (inc i)
                                    :cumulated cumulated}))
                               {:deltas deltas
                                :i 1
                                :cumulated 0}
                               deltas))))
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

(defn make
  ([] (Level. (sorted-map)))
  ([vals]
   (Level. (->> vals
                (into (sorted-map))))))
