(ns auto-opti.time-based.impl.aggregator
  "An `aggregator` aggregates `bucket` into `bucket-aggregate` and the way back.

  It is composed of `aggregator-item`, matching but completing the data `aggregate`.

  ![Aggregator entity diagram](archi/time_based/aggregator.png)"
  (:require
   [auto-opti.time-based.impl.aggregator-item :as opt-tb-aggregator-item]))

(defn- by-bucket
  "Find the `bucket` matching `aggregate`."
  [aggregator bucket]
  (-> (filter (partial opt-tb-aggregator-item/concerns-bucket? bucket) aggregator)
      first))

(defn- by-aggregate
  "Find the `aggregate` matching the `bucket`."
  [aggregator aggregate]
  (-> (filter (partial opt-tb-aggregator-item/concerns-bucket-aggregate? aggregate) aggregator)
      first))

(defn bucket-aggregate
  "Returns the `bucket-aggregate` matching `bucket` in the `aggregator`."
  [aggregator bucket]
  (some-> (by-bucket aggregator bucket)
          (opt-tb-aggregator-item/bucket-aggregate bucket)))

(defn bucket-aggregates
  "Returns the `bucket-aggregate` matching `buckets` in the `aggregator`.
  Note that each `bucket-aggregate` is returned once, and non `bucket` outside any aggregate are excluded."
  [aggregator buckets]
  (->> buckets
       (map (partial bucket-aggregate aggregator))
       distinct
       (filterv some?)))

(defn bucket-range
  "Returns the range of `bucket` matching a `bucket-aggregate` in the `aggregator`.
  Note the returned value is a pair which first element is included, second is excluded."
  [aggregator bucket-aggregate]
  (some-> (by-aggregate aggregator bucket-aggregate)
          (opt-tb-aggregator-item/bucket-range bucket-aggregate)))
