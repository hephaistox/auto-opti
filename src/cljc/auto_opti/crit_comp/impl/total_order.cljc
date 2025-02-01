(ns auto-opti.crit-comp.impl.total-order
  "Criteria comparators for total ordering - all elements are comparable and will be greater worst or equal.

  All returned value are comparison function taking `crit1` and `crit2` as parameter and returns:
  * a `negative` number if `crit1` is better than `crit2`
  * a `positive` number if `crit2` is better than `crit1`
  * `0` if `crit1` and `crit2` are equal."
  (:require
   [auto-opti.maths.weighted-sum :as opt-weighted-sum]))

(defn crit-map [crit-name] (fn [crit1 crit2] (compare (get crit1 crit-name) (get crit2 crit-name))))

(defn hierarchise
  "Scan all elements in order."
  [order]
  (fn [crit1 crit2]
    (loop [[f & r] order]
      (let [res (compare (get crit1 f) (get crit2 f))]
        (if (and (seq r) (zero? res)) (recur r) res)))))

(defn weighted-sum-comp
  [weights]
  (fn [crit1 crit2]
    (compare (opt-weighted-sum/weighted-sum weights crit1)
             (opt-weighted-sum/weighted-sum weights crit2))))

(defn smaller-comp [] (fn [crit1 crit2] (compare crit1 crit2)))

(defn bigger-comp [] (fn [crit1 crit2] (- (compare crit1 crit2))))
