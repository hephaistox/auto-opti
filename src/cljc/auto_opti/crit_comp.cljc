(ns auto-opti.crit-comp
  "Criteria comparator."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-opti.maths.weighted-sum :as opt-weighted-sum]))

(defn- cmp
  [crit1 crit2]
  (let [res (compare crit1 crit2)]
    (cond
      (zero? res) :crit-eq
      (neg? res) :crit1-better
      :else :crit1-worst)))

(def id "Criteria comparison name." :keyword)

(def schema
  [:map-of
   id
   [:map {:closed true}
    [:doc :string]
    [:params-schema :any]
    [:f [:function [:=> [:cat :any] :any]]]]])

(declare crit-comp-fn)

(def default-registry
  "The default `crit-comp` registry contains main criteria comparison implementations. See map's keys for a list of them."
  {:hierarchise {:doc "Hierarchise the criteria."
                 :params-schema [:map [:order [:vector :map]]]
                 :f (fn [{:keys [order]}]
                      (let [order (mapv (juxt crit-comp-fn :crit-name) order)]
                        (fn [crit1 crit2]
                          (loop [[[crit-comp-fn crit-name] & rorder] order]
                            (when crit-comp-fn
                              (let [res (crit-comp-fn (get crit1 crit-name) (get crit2 crit-name))]
                                (if (and (seq rorder) (= :crit-eq res)) (recur rorder) res)))))))}
   :weighted-sum {:doc "Weighted sum of criteria."
                  :params-schema [:map [:weights [:map-of :keyword [:or :int :double]]]]
                  :f (fn [{:keys [weights]}]
                       (fn [crit1 crit2]
                         (let [c (compare (opt-weighted-sum/weighted-sum weights crit1)
                                          (opt-weighted-sum/weighted-sum weights crit2))]
                           (cond
                             (zero? c) :crit-eq
                             (pos? c) :crit1-worst
                             (neg? c) :crit1-better))))}
   :smaller {:doc "The smaller criteria is the smaller one according to `compare` function."
             :params-schema :nil
             :f (fn [_]
                  (fn [crit1 crit2]
                    (let [c (compare crit1 crit2)]
                      (cond
                        (zero? c) :crit-eq
                        (pos? c) :crit1-worst
                        (neg? c) :crit1-better))))}
   :bigger {:doc "The bigger criteria is the bigger one according to `compare` function."
            :params-schema :nil
            :f (fn [_]
                 (fn [crit1 crit2]
                   (let [c (compare crit1 crit2)]
                     (cond
                       (zero? c) :crit-eq
                       (pos? c) :crit1-better
                       (neg? c) :crit1-worst))))}
   :strict {:doc "Strict comparison of multi criteria."
            :params-schema [:map [:crit-names [:vector :keyword]]]
            :f (fn [{:keys [crit-names]}]
                 (fn [crit1 crit2]
                   (let [{:keys [crit1-worst crit1-better]
                          :or {crit1-worst 0
                               crit1-better 0}}
                         (->> crit-names
                              (map (fn [k] (cmp (get crit1 k) (get crit2 k))))
                              frequencies)]
                     (cond
                       (and (zero? crit1-worst) (zero? crit1-better)) :crit-eq
                       (zero? crit1-worst) :crit1-better
                       (zero? crit1-better) :crit1-worst
                       :else :crit-nc))))}})

(defn crit-comp-fn
  "Build a `comparator` with a registry and a `crit-comp-name` and return a ready to use function."
  [{:keys [registry crit-comp-name]
    :or {registry default-registry}
    :as crit-comp-pars}]
  (when-let [f (-> registry
                   (get crit-comp-name)
                   (get :f))]
    (f crit-comp-pars)))

(defn direct-eval
  "Helper to compare `crit1` and `crit2` based on map describing the `crit-comp-pars`. For test only, crit-comp-fn once the crit-comp and use it multiple times instead."
  [crit-comp-pars crit1 crit2]
  (when-let [f (-> crit-comp-pars
                   crit-comp-fn)]
    (f crit1 crit2)))

