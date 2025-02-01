(ns auto-opti.time-based.impl.var-latest
  "A `tb-var` that stores a measure `m` done at bucket `b` that is true for all subsequent buckets after `b` unless a new measure is done, at a later `bucket`.

  * Each update of an already existing value is overwritten.
  * The get-measure is returning the value at bucket `b` or the value at the earlier non nil date (as this latest is still available according to this time based).
  * The values can be whatever, as no operation is done on it.

  For instance, a stock level is such, since a new stock level will be recorded at that moment.
  Note that the order of the measures is not significant.

  ![tb var latest entity diagram](archi/time_based/tb_var_latest.png)"
  (:require
   [auto-opti.time-based.impl.storage-strategy :as opt-tb-ss]
   [auto-opti.time-based.protocol              :as opt-tb-protocol]))

(defrecord TbVarLatest [storage default-value]
  opt-tb-protocol/TimeBased
    (default [_] default-value)
    (get-measure [_ bucket]
      (if-let [val (opt-tb-ss/get-before storage bucket)]
        val
        default-value))
    (get-measures [this buckets] (mapv (fn [v] (opt-tb-protocol/get-measure this v)) buckets))
    (measure [_ bucket data]
      (TbVarLatest. (opt-tb-ss/assoc-date storage bucket data) default-value)))

(defn make [storage default-value] (->TbVarLatest storage default-value))
