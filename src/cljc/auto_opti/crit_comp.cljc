(ns auto-opti.crit-comp
  "Use criteria comparator to compare two solutions."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-opti                    :as-alias opti]
   [auto-opti.maths.weighted-sum :as opt-weighted-sum]))

(defn- cmp
  [crit1 crit2]
  (when (and (some? crit1) (some? crit2))
    (let [res (compare crit1 crit2)]
      (cond
        (zero? res) :equal
        (neg? res) :better
        :else :worst))))

(def id "Malli schema to name a criteria comparison." :keyword)

(def registry-schema
  "Malli schema for a criteria comparison's registry."
  [:map-of
   id
   [:map {:closed true}
    [:doc :string]
    [:params-schema :any]
    [:f [:function [:=> [:cat :any] :any]]]]])

(declare crit-comp-fn)

(def default-registry
  "By default, the registry contains most common implementations. See map's keys for their names, and check `:doc` in submap to have a more detailed description."
  {:hierarchise {:doc "Hierarchise the criteria, as defined in order."
                 :params-schema [:map [:order [:vector :map]]]
                 :f (fn [{::opti/keys [order]}]
                      (let [order (mapv (juxt crit-comp-fn ::opti/crit-name) order)]
                        (fn [crit1 crit2]
                          (loop [[[crit-comp-fn crit-name] & rorder] order]
                            (when crit-comp-fn
                              (let [res (crit-comp-fn (get crit1 crit-name) (get crit2 crit-name))]
                                (if (and (seq rorder) (= :equal res)) (recur rorder) res)))))))}
   :weighted-sum {:doc "Weighted sum of criteria."
                  :params-schema [:map [:weights [:map-of :keyword [:or :int :double]]]]
                  :f (fn [{::opti/keys [weights]}]
                       (let [weights
                             (into {} (remove (fn [[_ v]] (or (nil? v) (zero? v)))) weights)]
                         (fn [crit1 crit2]
                           (let [c (compare (opt-weighted-sum/weighted-sum weights crit1)
                                            (opt-weighted-sum/weighted-sum weights crit2))]
                             (cond
                               (zero? c) :equal
                               (pos? c) :worst
                               (neg? c) :better)))))}
   :smaller {:doc "The smaller criteria is the smaller one according to `compare` function."
             :params-schema :nil
             :f (fn [_]
                  (fn [crit1 crit2]
                    (if (and (some? crit1) (some? crit2))
                      (let [c (compare crit1 crit2)]
                        (cond
                          (zero? c) :equal
                          (pos? c) :worst
                          (neg? c) :better))
                      :nc)))}
   :bigger {:doc "The bigger criteria is the bigger one according to `compare` function."
            :params-schema :nil
            :f (fn [_]
                 (fn [crit1 crit2]
                   (if (and (some? crit1) (some? crit2))
                     (let [c (compare crit1 crit2)]
                       (cond
                         (zero? c) :equal
                         (pos? c) :better
                         (neg? c) :worst))
                     :nc)))}
   :strict {:doc "Strict comparison of multi criteria."
            :params-schema [:map [:crit-names [:vector :keyword]]]
            :f (fn [{::opti/keys [crit-names]}]
                 (fn [crit1 crit2]
                   (let [{:keys [worst better]
                          :or {worst 0
                               better 0}}
                         (->> crit-names
                              (map (fn [k] (cmp (get crit1 k) (get crit2 k))))
                              frequencies)]
                     (cond
                       (and (zero? worst) (zero? better)) :equal
                       (zero? worst) :better
                       (zero? better) :worst
                       :else :crit-nc))))}})

(defn crit-comp-fn
  "Build a `comparator` with a registry and a `crit-comp-name` and return a ready to use function."
  [{::opti/keys [registry crit-comp-name]
    :as crit-comp-pars}]
  (when-let [f (-> (or registry default-registry)
                   (get crit-comp-name)
                   (get :f))]
    (f crit-comp-pars)))
