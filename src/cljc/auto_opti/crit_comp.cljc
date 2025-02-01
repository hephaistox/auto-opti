(ns auto-opti.crit-comp
  "Criteria comparator."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-opti.crit-comp.registry :as opt-crit-comp-reg]))

(def id opt-crit-comp-reg/id)

(def default-registry opt-crit-comp-reg/registry)

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
