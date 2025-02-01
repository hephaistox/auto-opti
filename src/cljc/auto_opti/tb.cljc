(ns auto-opti.tb
  "Time based values.

  There are
  * `spot` the value is happening on a specific date
  * `latest`

  Values can be
  * `additive` so each measure is added to the previous one"
  (:refer-clojure :exclude [inc dec min max +])
  (:require
   [auto-opti.tb.additive :as opt-tb-additive]
   [auto-opti.tb.level    :as opt-tb-level]
   [auto-opti.tb.protocol :as opt-tb-prot]))

(defn make-additive [] (opt-tb-additive/make))

(defn make-level ([] (opt-tb-level/make)) ([vals] (opt-tb-level/make vals)))

(defn cumulative [tb-var] (opt-tb-prot/cumulative tb-var))

(defn moving-average [tb-var] (opt-tb-prot/moving-average tb-var))

(defn scalar-fn [tb-var s-fn] (opt-tb-prot/scalar-fn tb-var s-fn))

(defn measure [tb-var date data] (opt-tb-prot/measure tb-var date data))

(defn get-measure [tb-var date] (opt-tb-prot/get-measure tb-var date))

(defn clamp [tb-var start end] (opt-tb-prot/clamp tb-var start end))

(defn mean [tb-var start end] (opt-tb-prot/mean tb-var start end))

(defn stddev [tb-var] (opt-tb-prot/stddev tb-var))

(defn get-measures [tb-var] (opt-tb-prot/get-measures tb-var))

(defn inc
  [tb-var date]
  (->> (clojure.core/inc (get-measure tb-var date))
       (measure tb-var date)))

(defn dec
  [tb-var date]
  (->> (clojure.core/dec (get-measure tb-var date))
       (measure tb-var date)))

(defn min
  [tb-var]
  (let [ms (get-measures tb-var)] (when-not (empty? ms) (apply clojure.core/min ms))))

(defn max
  [tb-var]
  (let [ms (get-measures tb-var)] (when-not (empty? ms) (apply clojure.core/max ms))))

(defn + [tb-var] (let [ms (get-measures tb-var)] (when-not (empty? ms) (apply clojure.core/+ ms))))

(defn stats
  [tb-var start end]
  {:min (min tb-var)
   :vals (clamp tb-var start end)
   :max (max tb-var)
   :stddev (stddev tb-var)
   :mean (mean tb-var start end)})

(defn incnil
  "Returns a function that updates a `tb-var` to next value, at date `bucket`.

  If the tb-var is `nil`, it is created with type `tb-type`."
  [bucket tb-type]
  (fn [tb-var]
    (let [tb-var (or tb-var
                     (case tb-type
                       :level (make-level)
                       :additive (make-additive)))]
      (inc tb-var bucket))))

(defn decnil
  "Returns a function that updates a `tb-var` to next value, at date `bucket`.

  If the tb-var is `nil`, it is created with type `tb-type`."
  [bucket tb-type]
  (fn [tb-var]
    (let [tb-var (or tb-var
                     (case tb-type
                       :level (make-level)
                       :additive (make-additive)))]
      (dec tb-var bucket))))
