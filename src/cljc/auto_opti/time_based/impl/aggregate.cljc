(ns auto-opti.time-based.impl.aggregate
  "An `aggregate` is a value object used to define a group of `bucket`s that is homogeneously aggregated in `bucket-aggregate`.

  * `start-bucket` is the first `bucket` of the aggregate (`start-bucket ∈ ℕ`).
  * `end-bucket` is the last `bucket` concerned with that aggregate - `end-bucket` is excluded - (`end-bucket ∈ ℕ` or `nil`).
  * `step` is the number of `bucket`s gathered in that `aggregate`, `step  ∈ ℕ*`

  Note that without knowing the other `aggregate` in the `aggregates`, the targeted  `bucket-aggregate` are not fully defined."
  (:require
   [auto-opti :as-alias opti]))

(def schema
  [:map {:closed true}
   [::opti/start-bucket [:and :int [:>= 0]]]
   [::opti/end-bucket {:optional true}
    [:and :int [:>= 0]]]
   [::opti/step {:optional true}
    [:and :int [:> 0]]]])

(defn set-end-bucket
  "For an `aggregate`, its `end-bucket` is :
  * not modified is already set,
  * defaulted to `start-bucket` of the `next-aggregate` if set."
  [{::opti/keys [end-bucket]
    :as aggregate}
   {::opti/keys [start-bucket]
    :as _next-aggregate}]
  (cond-> aggregate
    (and (some? start-bucket) (nil? end-bucket)) (assoc ::opti/end-bucket start-bucket)))

(defn default
  "Default `step` to `1`."
  [{::opti/keys [step]
    :as aggregate}]
  (assoc aggregate ::opti/step (or step 1)))
