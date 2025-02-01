(ns auto-opti.routings
  "Jobshop data.
  Such a model a `routes` attributes mapping a `route-id` to an ordered list of operation.

  Each operation is made of:
  * `m` the machine name
  * `pt` the processing time that could be an integer or a distribution"
  (:require
   [auto-opti.dstb :as opt-dstb]))

(defn machines
  "Sorted list of machines"
  [{:keys [routes]
    :as _model}]
  (->> routes
       vals
       (map :operations)
       (apply concat)
       (map :m)
       distinct
       sort))

(defn start
  "Prepare `model` data to start routings.

  Data are turned into distributions.
  All distributions are using `prng`."
  [model prng]
  (let [{:keys [routes]} model
        dstb-fn (partial opt-dstb/dstb prng)]
    (-> model
        (assoc :route-dstb
               (dstb-fn {:dstb-name :categorical
                         :category-probabilities (update-vals routes #(get % :probability 1))}))
        (update :routes
                (fn [routes]
                  (->> routes
                       (mapv (fn [[route-id route]]
                               [route-id
                                (-> route
                                    (assoc :route-id route-id)
                                    (update :operations
                                            (fn [operations]
                                              (->> operations
                                                   (mapv (fn [operation]
                                                           (-> operation
                                                               (update :pt dstb-fn))))))))]))
                       (into {})))))))

(defn pick-route-id
  "Pick one route-id based on `route-dstb`"
  [model]
  (let [{:keys [route-dstb]} model] (when (seq (:routes model)) (opt-dstb/resolve route-dstb))))
