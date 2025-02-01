(ns auto-opti.time-based.impl.var-aggregated
  "A `tb-var` storing data through an aggregator.

  Note that the storage strategy and the way values are aggregated is the repsonsability of the `tb-var`.

  ![tb-var-aggregated entity diagram](archi/time_based/tb_var_aggregated.png)"
  (:require
   [auto-opti.time-based.impl.aggregator :as opt-tb-aggregator]
   [auto-opti.time-based.protocol        :as opt-tb-protocol]))

(defrecord TbVarAggregated [tb-var aggregator]
  opt-tb-protocol/TimeBased
    (default [_] (opt-tb-protocol/TimeBased tb-var))
    (get-measure [_ bucket]
      (when-let [bucket-aggregate (opt-tb-aggregator/bucket-aggregate aggregator bucket)]
        (opt-tb-protocol/get-measure tb-var bucket-aggregate)))
    (get-measures [_ buckets]
      (->> (opt-tb-aggregator/bucket-aggregates aggregator buckets)
           (opt-tb-protocol/get-measures tb-var)))
    (measure [this bucket value]
      (if-let [bucket-aggregate (opt-tb-aggregator/bucket-aggregate aggregator bucket)]
        (TbVarAggregated. (opt-tb-protocol/measure tb-var bucket-aggregate value) aggregator)
        this)))

(defn make [time-based translations] (->TbVarAggregated time-based translations))
