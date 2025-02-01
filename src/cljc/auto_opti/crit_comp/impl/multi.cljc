(ns auto-opti.crit-comp.impl.multi
  "Returns multi criteria evaluation where some criteria could be not comparable.")

(defn- cmp
  [crit1 crit2]
  (let [res (compare crit1 crit2)]
    (cond
      (zero? res) :eq
      (neg? res) :crit1-better
      :else :crit1-worst)))

(defn strict
  "Returns a strict comparison fn for criteria `crit-names`.

  The comparison function takes `crit1` and `crit2` as parameter and returns:
  * `:better` if `crit1` less than `crit2`
  * `:eq` if `crit1` equal to `crit2`
  * `:worst` if `crit1` greater than `crit2`
  * `:nc` if not comparable"
  ([crit-names]
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
         :else :nc)))))
