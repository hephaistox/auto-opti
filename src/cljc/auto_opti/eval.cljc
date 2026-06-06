(ns auto-opti.eval
  "Optimisation evalutions. This namespace contains some simple evalutions for test and demo purposes.

  It contains also a `registry` mechanism that users should enrich."
  (:refer-clojure :exclude [eval])
  (:require
   [auto-opti                 :as-alias opti]
   [auto-opti.eval.montecarlo :as opt-montecarlo]))

(def id "Malli schema to name an evalution" :keyword)

(def registry-schema
  "Malli schema for an evaluation's registry."
  [:map-of
   id
   [:map {:closed true}
    [::opti/doc :string]
    [::opti/rep-type :keyword]
    [::opti/valid-pars fn?]
    [::opti/eval-fn fn?]]])

(def registry
  "Registry of evaluations.

  In that map, check keywords for eval names, and `:auto-opti/doc` for their description."
  {:montecarlo-pi
   #::opti{:doc
           "`x` and `y` are drawn in an uniform distribution in `[-radius;radius]` interval.

  * So the points at coordinatess (x,y) are in a square of edge `2*radius` and surface `4*radius*radius`.
  * A point is in the circle where its distance from the center of the circle - the origin - is less that radius. This distance is (square root of x2 + y2)

  * So, if we assume the distribution is perfect, the distribution between the circle and the square are based on their surface:
      * square: total number of drawns -> 4*radius*radius
      * circle: number of drawns with distance less than radius from the origin -> π*radius*radius

  So dividing the total number of drawns regarding the numbers in the circle, we have:

  * (nb-in / nb-total) = (/ π*radius*radius 4*radius*radius) = π/4
  * π = 4*(nb-in/nb-total)"
           :rep-type :seed
           :valid-pars opt-montecarlo/valid-pars
           :eval-fn opt-montecarlo/eval}})

(defn eval-map
  "Build an evaluation for keyword
  
  Returns a function that evaluates a unique parameter - the representation and returns a solution."
  ([kw params iterator-fn model] (eval-map kw registry params iterator-fn model))
  ([kw registry params iterator-fn model]
   (let [{::opti/keys [eval-fn valid-pars]} (get registry kw)]
     (if valid-pars
       (let [err (valid-pars params iterator-fn model)]
         (if err err (partial eval-fn params iterator-fn model)))
       {:error :not-found
        :kw kw}))))


