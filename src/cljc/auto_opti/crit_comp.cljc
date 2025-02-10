(ns auto-opti.crit-comp
  "Criteria comparator."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-opti.maths.weighted-sum :as opt-weighted-sum]))

(defn- cmp
  [crit1 crit2]
  (let [res (compare crit1 crit2)]
    (cond
      (zero? res) :eq
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

(def default-registry
  {:crit-map {:doc "Comparator of two criteria regarding their `crit-name`"
              :params-schema [:map [:crit-name :keyword]]
              :f (fn [{:keys [crit-name]}]
                   (fn [crit1 crit2] (compare (get crit1 crit-name) (get crit2 crit-name))))}
   :hierarchise {:doc "Hierarchise the criteria."
                 :params-schema [:map [:order [:vector :any]]]
                 :f (fn [{:keys [order]}]
                      (fn [crit1 crit2]
                        (loop [[f & r] order]
                          (let [res (compare (get crit1 f) (get crit2 f))]
                            (if (and (seq r) (zero? res)) (recur r) res)))))}
   :weighted-sum {:doc "Weighted sum of criteria."
                  :params-schema [:map [:weights [:map-of :keyword [:or :int :double]]]]
                  :f (fn [{:keys [weights]}]
                       (fn [crit1 crit2]
                         (compare (opt-weighted-sum/weighted-sum weights crit1)
                                  (opt-weighted-sum/weighted-sum weights crit2))))}
   :smaller {:doc "The smaller criteria is the smaller one according to `compare` function."
             :params-schema :nil
             :f (fn [_] (fn [crit1 crit2] (compare crit1 crit2)))}
   :bigger {:doc "The bigger criteria is the bigger one according to `compare` function."
            :params-schema :nil
            :f (fn [_] (fn [crit1 crit2] (- (compare crit1 crit2))))}
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
                       (and (zero? crit1-worst) (zero? crit1-better)) :eq
                       (zero? crit1-worst) :crit1-better
                       (zero? crit1-better) :crit1-worst
                       :else :nc))))}})

(defn build
  "Build a `comparator` with a registry and a `crit-comp-name`"
  [{:keys [registry crit-comp-name]
    :or {registry default-registry}
    :as crit-comp}]
  (when-let [f (-> registry
                   (get crit-comp-name)
                   (get :f))]
    (f crit-comp)))

(defn direct-eval
  "Compare `crit1` and `crit2` with `crit-comp` data."
  [crit-comp crit1 crit2]
  (when-let [f (-> crit-comp
                   build)]
    (f crit1 crit2)))
