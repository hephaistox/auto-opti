(ns auto-opti.time-based.impl.aggregator-item
  "An `aggregator-item` enriches an `aggregate` with `start-bucket-aggregate` and `end-bucket-aggregate`.
  Many `aggregator-item` are contained in an `aggregator`.

  Note that no test mechanisms are here to check values of the `aggregator-item`, the `aggregate` and `aggregates` are here to deal with these tests and are responsible to built some valid `aggregator-item`.

  ![Aggregator-item entity diagram](archi/time_based/aggregator-item.png)")

(defn- bucket-aggregate*
  "`bucket-aggregate` formula based on a `aggregator`."
  [{:auto-opti.time-based/keys [start-bucket step start-bucket-aggregate]
    :as _aggregator}
   bucket]
  (+ start-bucket-aggregate (quot (- bucket start-bucket) step)))

(defn bucket-aggregate
  "Returns the `bucket-aggregate` matching `bucket` in the `aggregator`."
  [{:auto-opti.time-based/keys [start-bucket end-bucket]
    :as aggregator}
   bucket]
  (when (or (nil? end-bucket) (and (<= start-bucket bucket) (< bucket end-bucket)))
    (bucket-aggregate* aggregator bucket)))

(defn bucket-range
  "Returns the range of `bucket` matching `bucket-aggregate` in the `aggregator`."
  [{:auto-opti.time-based/keys [start-bucket step start-bucket-aggregate end-bucket-aggregate]
    :as _aggregator}
   bucket-aggregate]
  (when (or (nil? end-bucket-aggregate)
            (and (<= start-bucket-aggregate bucket-aggregate)
                 (< bucket-aggregate end-bucket-aggregate)))
    (let [start-bucket-range (+ start-bucket (* step (- bucket-aggregate start-bucket-aggregate)))]
      [start-bucket-range (+ start-bucket-range step)])))

(defn calculate-end-bucket-aggregate
  "Calculate the `end-bucket-aggregate` field in `aggregator`."
  [{:auto-opti.time-based/keys [end-bucket]
    :as aggregator}]
  (cond-> aggregator
    (some? end-bucket) (assoc :auto-opti.time-based/end-bucket-aggregate
                              (bucket-aggregate* aggregator end-bucket))))

(defn build
  "Buids and returns an `aggregator`, based on `start-bucket-aggregate` `start-bucket` `end-bucket` `step`."
  [start-bucket-aggregate aggregator]
  (-> aggregator
      (assoc :auto-opti.time-based/start-bucket-aggregate start-bucket-aggregate)
      calculate-end-bucket-aggregate))

(defn bucket-to-bucket-aggregate
  "Returns an array matching the `bucket` to its `bucket-aggregate` based on `aggregator` definition.
  First element in the array is the `bucket-aggregate` of `start-bucket`, next is the `bucket-aggregate` of `start-bucket+1`, ..."
  [{:auto-opti.time-based/keys
    [start-bucket-aggregate step end-bucket-aggregate start-bucket end-bucket]
    :as _aggregator}]
  (take (- end-bucket start-bucket)
        (mapcat (partial repeat step) (range start-bucket-aggregate end-bucket-aggregate))))

(defn concerns-bucket?
  "Returns `true` if the `bucket` is between `start-bucket` and `end-bucket`.
   Note that the `start-bucket` field is mandatory in the definition, but the `end-bucket` field is optional.

   If `end-bucket` is `nil`, it is interpretated as infinite."
  [bucket
   {:auto-opti.time-based/keys [start-bucket end-bucket]
    :as _aggregator}]
  (and (<= start-bucket bucket) (or (nil? end-bucket) (< bucket end-bucket))))

(defn concerns-bucket-aggregate?
  "Returns `true` if the `bucket-aggregate` is in the range of the `bucket-aggregate` definition. So it is between fields:

   * the `start-bucket-aggregate` field is mandatory in the definition
   * the `end-bucket-aggregate` field is optional as the aggregate could end at infinite"
  [bucket-aggregate
   {:auto-opti.time-based/keys [start-bucket-aggregate end-bucket-aggregate]
    :as _aggregator}]
  (and (<= start-bucket-aggregate bucket-aggregate)
       (or (nil? end-bucket-aggregate) (< bucket-aggregate end-bucket-aggregate))))
