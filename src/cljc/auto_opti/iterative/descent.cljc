(ns auto-opti.iterative.descent
  "EXPERIMENTAL / work-in-progress — **not part of the public API**.

  Intended to host a descent algorithm: scan the neighborhood of a representation,
  keep the best neighbor according to a criteria comparator, and iterate.

  The implementation is **unfinished and not functional yet**: the descent loop is
  only a sketch (see the `comment` block below) and `stochastic` currently throws.
  Do not rely on this namespace; its API may change or be removed entirely."
  {:no-doc true})

(defn stochastic
  "EXPERIMENTAL — not implemented yet.

  Intended to stochastically scan the neighborhood to optimize the representation.

  Throws an `ex-info`: the descent loop below is an unfinished sketch and is not
  ready for use. See the `comment` block in this namespace for the work-in-progress
  intent."
  [starting-rep nb-iterations neighbor-fn crit-comp tx ty evals]
  (throw (ex-info "auto-opti.iterative.descent/stochastic is not implemented yet"
                  {:starting-rep starting-rep
                   :nb-iterations nb-iterations
                   :neighbor-fn neighbor-fn
                   :crit-comp crit-comp
                   :tx tx
                   :ty ty
                   :evals evals})))

(comment
  ;; Work-in-progress sketch of the descent loop, preserved for the future author.
  ;;
  ;; Known issues to resolve before this can be promoted out of quarantine:
  ;; - `best` is bound to nil yet later dereferenced via `(:rep best)` and used as a
  ;;   comparison baseline; it must be seeded with the evaluated starting transfo.
  ;; - the stray `(copy-rep)` call inside the loop has no arguments and does nothing
  ;;   useful; it should be removed or given the representation to copy.
  ;; - `ty`/`_ty` is built once but never consumed by the loop; decide whether the
  ;;   algorithm needs a second working transfo or drop the parameter.
  ;; - the `recur` branches alternate between the new neighbor `y` and `best`, but the
  ;;   accumulator/`tx` returned on completion is not kept in sync with `best`.
  ;;
  ;; `evals` is expected to provide:
  ;;   :copy-rep  (fn [src dst]    -> rep)   copy a representation
  ;;   :eval-sol  (fn [rep sol]    -> sol)   evaluate a representation into a solution
  ;;   :eval-crit (fn [sol crit]   -> crit)  evaluate a solution into its criteria
  ;; A "transfo" is a map {:rep ... :sol ... :crit ...}.
  (defn stochastic-sketch
    [starting-rep nb-iterations neighbor-fn crit-comp tx ty evals]
    (let [{:keys [copy-rep eval-sol eval-crit]} evals
          eval-transfo (fn [{:keys [rep sol crit]
                             :as transfo}]
                         (eval-sol rep sol)
                         (eval-crit sol crit)
                         transfo)
          best nil ;;TODO seed `best` with the evaluated starting transfo
          _ty (-> ty
                  (update :rep #(copy-rep starting-rep %))
                  eval-transfo)]
      (loop [current-iteration 0
             tx (-> tx
                    (update :rep #(copy-rep starting-rep %))
                    eval-transfo)]
        (copy-rep) ;;TODO stray no-arg call, remove or supply the representation
        (let [y (-> best
                    :rep
                    copy-rep
                    neighbor-fn
                    eval-transfo)]
          (cond
            (> current-iteration nb-iterations) {:best tx
                                                 :nb-iterations nb-iterations}
            (crit-comp (:crit y) (:crit best)) (recur (inc current-iteration) y)
            :else (recur (inc current-iteration) best)))))))
