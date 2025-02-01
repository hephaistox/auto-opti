(ns auto-opti.time-based.impl.var-additive
  "`tb-var` that stores additive informations over time buckets.

  * `default-value` is the default value for new or empty buckets, it should be the \"`0`\".
  * If a value is set to an empty bucket, the value is replacing the `default-value`.
  * If a value is already stored in that bucket, it will be added to the existing value.
  * The values should be numerical to support +.

  ![Aggregate entity diagram](archi/time_based/tb_var_additive.png)"
  (:require
   [auto-opti.time-based.impl.storage-strategy :as opt-tb-ss]
   [auto-opti.time-based.protocol              :as opt-tb-protocol]))

(defrecord TbVarAdditive [storage default-value]
  opt-tb-protocol/TimeBased
    (default [_] default-value)
    (get-measure [_ bucket]
      (if-let [val (opt-tb-ss/get-exact storage bucket)]
        val
        default-value))
    (get-measures [_ buckets]
      (mapv #(if (nil? %) default-value %) (opt-tb-ss/get-measures storage buckets)))
    (measure [_ bucket data]
      (TbVarAdditive. (opt-tb-ss/update-date storage
                                             bucket
                                             (fn [val]
                                               (if (nil? val) (+ data default-value) (+ val data)))
                                             [])
                      default-value)))

(def ^:private zero-plus "Zero for the plus operator" 0)

(defn make
  ([storage-strategy] (->TbVarAdditive storage-strategy zero-plus))
  ([storage-strategy default-value] (->TbVarAdditive storage-strategy default-value)))
