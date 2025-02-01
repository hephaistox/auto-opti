(ns auto-opti.dstb.impl.category
  "A category distribution."
  (:require
   [auto-opti.dstb.dstb-protocol :as opt-dstb-prot]
   [auto-opti.prng.stateful      :as opt-prng-stateful]))

(defn pick-category
  [value categories]
  (loop [value value
         [[k v] & rcategories] categories]
    (cond
      (< value 0) nil
      (< value v) k
      (empty? rcategories) nil
      :else (recur (- value v) rcategories))))

(defrecord Category [prng categories total-weight]
  opt-dstb-prot/Distribution
    (draw [_]
      (when (pos? total-weight)
        (-> prng
            (opt-prng-stateful/rnd-double 0 total-weight)
            (pick-category categories))))
    (median [_]
      (-> (/ total-weight 2)
          (pick-category categories)))
    (cumulative [_ _] nil)
    (minimum [_] (ffirst categories))
    (maximum [_] (first (last categories)))
    (quantile [_ p] p))

(defn make
  [prng categories]
  (let [total-weight (->> categories
                          (map second)
                          (apply +))]
    (->Category prng categories total-weight)))
