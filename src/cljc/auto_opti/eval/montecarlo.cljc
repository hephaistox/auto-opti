(ns auto-opti.eval.montecarlo
  "Evaluate an approximation of `π` with the montecarlo method."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-core.schema       :as opt-schema]
   [auto-opti.distribution :as opt-dstb]))

(defn eval
  "`x` and `y` are drawn in an uniform distribution in `[-radius;radius]` interval.

  * So the points at coordinatess (x,y) are in a square of edge `2*radius` and surface `4*radius*radius`.
  * A point is in the circle where its distance from the center of the circle - the origin - is less that radius. This distance is (square root of x2 + y2)

  * So, if we assume the distribution is perfect, the distribution between the circle and the square are based on their surface:
      * square: total number of drawns -> 4*radius*radius
      * circle: number of drawns with distance less than radius from the origin -> π*radius*radius

  So dividing the total number of drawns regarding the numbers in the circle, we have:

  * (nb-in / nb-total) = (/ π*radius*radius 4*radius*radius) = π/4
  * π = 4*(nb-in/nb-total)"
  [_params
   iterator-fn
   {:keys [radius iterations]
    :as _model}
   {:keys [seed]
    :as _rep}]
  (let [radius-square (* radius radius)
        unif-dst (opt-dstb/distribution #::opt-dstb{:dstb-name :uniform
                                                    :seed seed
                                                    :a (- radius)
                                                    :b radius})]
    (when (number? iterations)
      (loop [it 0
             nb-in 0]
        (when (fn? iterator-fn)
          (iterator-fn {:nb-in nb-in
                        :it it
                        :intermediate-criteria (when (pos? it)
                                                 (-> (/ nb-in it)
                                                     (* 4.0)))
                        :iterations iterations}))
        (let [x (opt-dstb/draw unif-dst)
              y (opt-dstb/draw unif-dst)
              in-circle? (<= (+ (* x x) (* y y)) radius-square)]
          (if (< it iterations)
            (recur (inc it) (if in-circle? (inc nb-in) nb-in))
            (-> (/ nb-in (inc it))
                (* 4.0))))))))

(def mc-model-schema [:map [:radius :int] [:iterations :int]])

(defn valid-pars
  "Returns `nil` if valid, a `map` if errors are found."
  [_params _iterator-fn model]
  (let [mv (opt-schema/validate-data-humanize mc-model-schema model)]
    (cond-> nil
      mv (assoc :model mv))))

