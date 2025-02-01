(ns auto-opti.time-based.impl.storage-strategy.contiguous
  "Stores data in a contiguous data structure.

  Adding an element outside the initial capacity will add chunk-size element, plus what's missing to store the element."
  (:require
   [auto-opti.time-based.impl.storage-strategy :as opt-tb-ss]))

(defn continuous-accept-k
  "Adds necessary slots so position `k` is in the `contiguous` datastructure.
  Size is increased with at least `chunk-size` element after it."
  [contiguous k chunk-size]
  (vec (cond-> contiguous
         (>= k (count contiguous)) (concat (repeat (+ chunk-size 1 (- k (count contiguous)))
                                                   nil)))))

(defrecord ContiguousStrategy [contiguous n chunk-size]
  opt-tb-ss/BucketData
    (assoc-date [this k v]
      (let [new-contiguous (continuous-accept-k contiguous k chunk-size)]
        (ContiguousStrategy. (assoc new-contiguous k v)
                             (if (nil? (opt-tb-ss/get-exact this k)) (inc n) n)
                             chunk-size)))
    (capacity [_] (count contiguous))
    (get-after [_ k]
      (->> (map (partial get contiguous) (range k (count contiguous)))
           (filter some?)
           first))
    (get-before [_ k]
      (->> (map (partial get contiguous) (range k -1 -1))
           (filter some?)
           first))
    (get-exact [_ k] (get contiguous k))
    (get-measures [this buckets] (map (fn [k] (opt-tb-ss/get-exact this k)) buckets))
    (nb-set [_] n)
    (range-dates [this] [(->> (range 0 (count contiguous))
                              (filter (partial opt-tb-ss/get-exact this))
                              first)
                         (->> (range (dec (count contiguous)) -1 -1)
                              (filter (partial opt-tb-ss/get-exact this))
                              first)])
    (update-date [_ k f args]
      (if-let [v (second (find contiguous k))]
        (ContiguousStrategy. (assoc contiguous k (apply f v args)) n chunk-size)
        (ContiguousStrategy. (assoc contiguous k (apply f nil args)) (inc n) chunk-size))))

(defn make [n] (->ContiguousStrategy (vec (repeat n nil)) 0 2))
