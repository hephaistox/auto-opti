(ns auto-opti.prng.impl.xoroshiro128
  "PRNG stateless implementation based on `xoroshoshiro128` algorithm.

  This prng is executing nearly the same code in `clj` and `cljs`. And it is an objective that this namespace returns exactly the same values for both clj and cljs.

  Warning. Long type in cljs doesn't exist so a number like this -3701927706170739138 is turned into a floating number that is loosing some precision. So the same seed (i.e. -3701927706170739138) seems to lead to different results (between clj and cljs).

  See [xoroshiro128 repo](https://github.com/thedavidmeister/xoroshiro128] for more details."
  (:refer-clojure :exclude [next])
  (:require
   [auto-opti.maths                 :as opt-maths]
   [auto-opti.prng.stateful-wrapper :as opt-stateful-wrapper]
   [auto-opti.prng.stateless        :as opt-prng-stateless]
   [cljc-long.core]
   [xoroshiro128.core               :as xoro]))

(defrecord Xoroshiro128 [xoro-object uuid-seed]
  opt-prng-stateless/PRNGStateless
    (jump [this] (update this :xoro-object xoro/jump))
    (uuid-seed [_] uuid-seed)
    (next [this] (update this :xoro-object xoro/next))
    (peek-int [_ min-int max-int]
      (let [r (- max-int min-int)
            m (-> (xoro/value xoro-object)
                  (cljc-long.core/mod (cljc-long.core/long r))
                  long)]
        (if (>= m 0) (+ min-int m) (+ min-int r m))))
    (peek-double [_ a b] (+ a (* (opt-maths/long->unit-double (xoro/value xoro-object)) (- b a)))))

(defn make-stateless
  "With the optional `uuid` parameter used as a seed, the stateless version of the xoroshiro prng is generated."
  ([uuid-seed]
   (when (uuid? uuid-seed)
     (->Xoroshiro128 (xoro/xoroshiro128+ (xoro/uuid->seed128 uuid-seed)) uuid-seed)))
  ([]
   (let [uuid-seed (random-uuid)]
     (->Xoroshiro128 (xoro/xoroshiro128+ (xoro/uuid->seed128 uuid-seed)) uuid-seed))))

(defn make
  "With the optional `uuid` parameter used as a seed, the stateful version of the xoroshiro prng is generated."
  ([uuid-seed] (opt-stateful-wrapper/make (make-stateless uuid-seed)))
  ([] (opt-stateful-wrapper/make (make-stateless))))
