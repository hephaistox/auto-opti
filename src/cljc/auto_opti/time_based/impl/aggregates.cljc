(ns auto-opti.time-based.impl.aggregates
  "Aggregates defines how an `aggregator` will be able to turn a `bucket` into a `bucket-aggregate`.
  It is a list of `aggregate` automatically sorted with their `start-bucket`.
  This list is used by a `aggregator-item` to create the `aggregator`.

  See the [[aggregator]] function, for details about the aggregation rules.

  ![Aggregates entity diagram](archi/time_based/aggregates.png)"
  (:require
   [auto-core.schema                          :as core-schema]
   [auto-opti.time-based.impl.aggregate       :as opt-tb-aggregate]
   [auto-opti.time-based.impl.aggregator-item :as opt-tb-aggregator-item]))

(defn- sort-start-bucket
  "Sort the `aggregates` with their `start-bucket`."
  [aggregates]
  (->> aggregates
       (sort-by :auto-opti.time-based/start-bucket)))

(defn- set-end-bucket
  "Set the `end-bucket` of an `aggregate` knowing the following `aggregate` in the order in `aggregtes`."
  [aggregates]
  (->> (interleave aggregates (conj (vec (rest aggregates)) nil))
       (partition-all 2)
       (mapv (fn [[aggregate next-aggregate]]
               (opt-tb-aggregate/set-end-bucket aggregate next-aggregate)))))

(defn validate
  "Is empty if `aggregates` is valid.
  Returns errors otherwise."
  [aggregates]
  (let [validations (core-schema/validate-data-humanize [:maybe
                                                         [:sequential opt-tb-aggregate/schema]]
                                                        aggregates)]
    (when-not (empty? validations) validations)))

(defn default-start-bucket-of-first-aggregate
  [aggregates]
  (-> aggregates
      (update 0
              (fn [{:auto-opti.time-based/keys [start-bucket]
                    :as aggregate}]
                (cond-> aggregate
                  (nil? start-bucket) (assoc :auto-opti.time-based/start-bucket 0))))))

(defn remove-aggregate-wo-start-bucket
  [aggregates]
  (->> aggregates
       (filter (comp some? :auto-opti.time-based/start-bucket))))

(defn make-aggregator
  "Creates the `aggregator` based on the `aggregates`.

  * The `aggregates` will be sorted with their `start-bucket`.
  * Default values will be added.
  * If the `end-bucket` of an aggregate is nil, it is defaulted to the `start-bucket` of the next one.

  Then, for the first `aggregate`:

  * `start-bucket-aggregate` starts to `0`.
  * `end-bucket-aggregate` is calculated based on `step` and `start-bucket-aggregate`.

  For next `aggregate`s:

  * the `start-bucket-aggregate` is the `end-bucket-aggregate` of the previous `aggregate`.
  * `end-bucket-aggregate` is calculated in the same way, except for the last one that my be `nil` if no end is defined."
  [aggregates]
  (->> aggregates
       sort-start-bucket
       remove-aggregate-wo-start-bucket
       (mapv opt-tb-aggregate/default)
       set-end-bucket
       (reduce (fn [[start-bucket-aggregate translations] aggregate]
                 (let [aggregator-item (opt-tb-aggregator-item/build start-bucket-aggregate
                                                                     aggregate)
                       {:auto-opti.time-based/keys [end-bucket-aggregate]} aggregator-item]
                   [end-bucket-aggregate (conj translations aggregator-item)]))
               [0 []])
       second))
