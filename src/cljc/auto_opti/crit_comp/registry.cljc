(ns auto-opti.crit-comp.registry
  "The registry of criteria comparison."
  (:require
   [auto-opti.crit-comp.impl.multi       :as opt-multi-crit]
   [auto-opti.crit-comp.impl.total-order :as opt-total-order]))

(def id "Criteria comparison name." :keyword)

(def schema
  [:map-of
   id
   [:map {:closed true}
    [:doc :string]
    [:params-schema :any]
    [:f [:function [:=> [:cat :any] :any]]]]])

(def registry
  {:crit-map
   {:doc
    "If the criteria is a map, the `crit-map` comparison compares the value of the `crit-name` field"
    :params-schema [:map [:crit-name :any]]
    :f (fn [{:keys [crit-name]}] (opt-total-order/crit-map crit-name))}
   :hierarchise
   {:doc
    "Hierarchise the criteria. The parameter is a list of criteria name, each criteria name is scanned."
    :params-schema [:map [:order [:vector :any]]]
    :f (fn [{:keys [order]}] (opt-total-order/hierarchise order))}
   :weighted-sum
   {:doc "Weighted sum of criteria, the parameter is a map associating a weight to each keyword."
    :params-schema [:map [:weights [:map-of :keyword [:or :int :double]]]]
    :f (fn [{:keys [weights]}] (opt-total-order/weighted-sum-comp weights))}
   :smaller {:doc "The smaller criteria is the smaller one according to `compare` function."
             :params-schema :nil
             :f (fn [_] (opt-total-order/smaller-comp))}
   :bigger {:doc "The bigger criteria is the bigger one according to `compare` function."
            :params-schema :nil
            :f (fn [_] (opt-total-order/bigger-comp))}
   :strict {:doc "Strict comparison of multi criteria"
            :params-schema [:map [:crit-names [:vector :keyword]]]
            :f (fn [{:keys [crit-names]}] (opt-multi-crit/strict crit-names))}})
