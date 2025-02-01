(ns auto-opti.time-based
  "`time-based` stores a `value` at a `bucket` in time.

  A `tb-var` is a variable than stores one `value` for each `bucket`. You can choose among the following variant `tb-var-additive-deltas`, `tb-var-additive-contiguous`, `tb-var-latest-deltas`, `tb-var-latest-contiguous`, or even build you own as it implements the `opt-tb-protocol`.

  The \"`bucket`\" concept is a natural integer (starting at `0`) representing the time in the internals of the simulation.
  ![Bucket entity diagram](archi/time_based/bucket.png)

  Some `bucket` of a `tb-var` can be grouped in `bucket-aggregate`, note that this `bucket-aggregate` will be seen as the `bucket` of the newly created `tb-var`.
  ![Bucket aggregate entity diagram](archi/time_based/bucket-aggregate.png)"
  (:require
   [auto-opti.time-based.impl.aggregates                  :as opt-tb-aggregates]
   [auto-opti.time-based.impl.aggregator                  :as opt-tb-aggregator]
   [auto-opti.time-based.impl.storage-strategy.contiguous :as opt-tb-contiguous]
   [auto-opti.time-based.impl.storage-strategy.deltas     :as opt-tb-deltas]
   [auto-opti.time-based.impl.var-additive                :as opt-tb-var-additive]
   [auto-opti.time-based.impl.var-aggregated              :as opt-tb-var-aggregated]
   [auto-opti.time-based.impl.var-latest                  :as opt-tb-var-latest]
   [auto-opti.time-based.protocol                         :as opt-tb-protocol]))

(defn tb-var-additive-deltas
  "Creates an `additive` `tb-var` storing the values with `deltas`.

  Data that are additive over buckets:

  * `default-value` is the default value for new or empty buckets, it should be the \"`0`\" of your `+` operator.
  * If a value is set to an empty bucket, the value is replacing the `default-value`.
  * If a value is already stored in that bucket, it will be added to the existing value.
  * The values should be numerical to support +.

  For instance, a production is additive, since a new production recorded in the same scenario-time is added to the previous auto-opti.time-based.impl.additive.

  This form is optimized for variables that has few values over the horizon, as it is storing only deltas, so only change in the value."
  ([default-value] (opt-tb-var-additive/make (opt-tb-deltas/make) default-value))
  ([] (opt-tb-var-additive/make (opt-tb-deltas/make))))

(defn tb-var-additive-contiguous
  "Creates an `additive` `tb-var` storing the values with `contiguous`.

  Data that are additive over buckets:

  * `default-value` is the default value for new or empty buckets, it should be the \"`0`\".
  * If a value is set to an empty bucket, the value is replacing the `default-value`.
  * If a value is already stored in that bucket, it will be added to the existing value.
  * The values should be numerical to support +.

  For instance, a production is additive, since a new production recorded in the same scenario-time is added to the previous auto-opti.time-based.impl.additive.

  This form is optimized for variables that has values all over the horizon, as it is storing in contiguous data structure."
  ([default-value size] (opt-tb-var-additive/make (opt-tb-contiguous/make size) default-value))
  ([default-value] (opt-tb-var-additive/make (opt-tb-contiguous/make 10) default-value))
  ([] (opt-tb-var-additive/make (opt-tb-contiguous/make 10))))

(defn tb-var-latest-deltas
  "A `tb-var-latest` is a time-based variable that, when a measure is set at bucket `b`, is setting all further buckets to this value unless another measure is done in between.

  * `default-value` is the default value if no value is set for a bucket nor for previous ones.
  * If a value is set to an empty bucket, the value is replacing the `default-value` for this bucket and all further ones.
  * If a value is stored to the same bucket, the previous value is replaced with that new one.
  * If a value is stored for `b2`, a further bucket in the horizon, the former value stay valid until `b2`.
  * The values can be whatever, as no operation is done on it.

  For instance, a stock level is such, since a new stock level will be recorded at that moment.

  This form particularly is optimized for variables that has few values over the horizon, as it is storing deltas."
  ([] (tb-var-latest-deltas nil))
  ([default-value] (opt-tb-var-latest/make (opt-tb-deltas/make) default-value)))

(defn tb-var-latest-contiguous
  "A `tb-var-latest` is a time-based variable that, when a measure is set at bucket `b`, is setting all further buckets to this value unless another measure is done in between.

  * `default-value` is the default value if no value is set for a bucket nor for previous ones.
  * If a value is set to an empty bucket, the value is replacing the `default-value` for this bucket and all further ones.
  * If a value is stored to the same bucket, the previous value is replaced with that new one.
  * If a value is stored for `b2`, a further bucket in the horizon, the former value stay valid until `b2`.
  * The values can be whatever, as no operation is done on it.

  For instance, a stock level is such, since a new stock level will be recorded at that moment.

  This form particularly is optimized for variables that has values all over the horizon, as it is storing in contiguous data strucure."
  ([default-value size] (opt-tb-var-latest/make (opt-tb-contiguous/make size) default-value))
  ([default-value] (opt-tb-var-latest/make (opt-tb-contiguous/make 10) default-value)))

(defn default
  "Default value before measurement in the `tb-var`."
  [tb-var]
  (opt-tb-protocol/default tb-var))

(defn get-measure
  "Returns the data stored at time `bucket`."
  [tb-var bucket]
  (opt-tb-protocol/get-measure tb-var bucket))

(defn get-measures
  "Returns the range of data in `[bucket-begin;bucket-end[`."
  [tb-var buckets]
  (opt-tb-protocol/get-measures tb-var buckets))

(defn measure
  "Returns a new instance of TimeBased with `data` measured at time `bucket`. The effect will depend on the kind of TimeBased."
  [tb-var bucket data]
  (opt-tb-protocol/measure tb-var bucket data))

(defn aggregator
  "Returns the `aggregator` based on the `aggregates`."
  [aggregates]
  (opt-tb-aggregates/make-aggregator aggregates))

(defn validate-aggregates [aggregates] (opt-tb-aggregates/validate aggregates))

(defn to-bucket-aggregate
  "Translate `bucket` to a `bucket-aggregate` thanks to the `aggregator`."
  [aggregator bucket]
  (opt-tb-aggregator/bucket-aggregate aggregator bucket))

(defn to-bucket
  "Translate bucket-aggregate to `bucket` thanks to the `aggregator`."
  [aggregator bucket-aggregator]
  (opt-tb-aggregator/bucket-range aggregator bucket-aggregator))

(defn to-bucket-aggregates
  "Translate a range of `bucket` to `bucket-aggregates` thanks to the `aggregator`."
  [aggregator buckets]
  (opt-tb-aggregator/bucket-aggregates aggregator buckets))

(defn tb-var-aggregated
  "Creates a `tb-var` using the `aggregator` to store the `values` with aggregateed `bucket`."
  [aggregator time-based]
  (opt-tb-var-aggregated/make time-based aggregator))
