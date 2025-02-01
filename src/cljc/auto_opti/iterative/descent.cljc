(ns auto-opti.iterative.descent "Descent algorithm, find the best among neighbors and iterate")

(defn stochastic
  "Scan stochastically the neighborhood to optimize the representation."
  [starting-rep nb-iterations neighbor-fn crit-comp tx ty evals]
  (let [{:keys [copy-rep eval-sol eval-crit]} evals
        eval-transfo (fn [{:keys [rep sol crit]
                           :as transfo}]
                       (eval-sol rep sol)
                       (eval-crit sol crit)
                       transfo)
        best nil
        ;;TODO
        _ty (-> ty
                (update :rep #(copy-rep starting-rep %))
                eval-transfo)]
    (loop [current-iteration 0
           tx (-> tx
                  (update :rep #(copy-rep starting-rep %))
                  eval-transfo)]
      (copy-rep)
      (let [y (-> best
                  :rep
                  copy-rep
                  neighbor-fn
                  eval-transfo)]
        (cond
          (> current-iteration nb-iterations) {:best tx
                                               :nb-iterations nb-iterations}
          (crit-comp (:crit y) (:crit best)) (recur (inc current-iteration) y)
          :else (recur (inc current-iteration) best))))))
