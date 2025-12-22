(ns auto-opti.routings-test
  (:require
   [auto-opti.distribution :as opt-dstb]
   [auto-opti.prng         :as opt-prng]
   [auto-opti.routings     :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest machines-test
  (is (= [:m1 :m2 :m3 :m4]
         (sut/machines #::sut{:routes {:blue {:operations [{:m :m4} {:m :m2} {:m :m1}]}
                                       :purple {:operations [{:m :m4} {:m :m3} {:m :m1}]}}}))
      "Extract machines"))

(deftest start-test
  (is (= #::sut{:routes {}
                :route-dstb {:categories {}
                             :total-weight 0}}
         (-> #::sut{:routes {}}
             (sut/start (opt-prng/prng #::opt-prng{}))
             (update ::sut/route-dstb #(into {} %))
             (update ::sut/route-dstb dissoc :prng)))
      "No route")
  (is
   (=
    #::sut{:routes {:a {:route-id :a
                        :probability 0.3
                        :operations [{:pt {:prng true
                                           :a 10
                                           :b 15
                                           :width 5.0}}]}
                    :b {:route-id :b
                        :probability 0.4
                        :operations [{:pt {:prng true
                                           :a 10
                                           :b 15
                                           :width 5.0}}]}}
           :route-dstb {:prng true
                        :categories {:a 0.3
                                     :b 0.4}
                        :total-weight 0.7}}
    (-> #::sut{:routes {:a {:route-id :a
                            :probability 0.3
                            :operations [{:pt #::opt-dstb{:dstb-name :uniform
                                                          :a 10
                                                          :b 15}}]}
                        :b {:route-id :a
                            :probability 0.4
                            :operations [{:pt #::opt-dstb{:dstb-name :uniform
                                                          :a 10
                                                          :b 15}}]}}}
        (sut/start (opt-prng/prng {}))
        (update ::sut/route-dstb #(into {} %))
        (update-in [::sut/route-dstb :prng] some?)
        (update ::sut/routes
                update-vals
                (fn [route]
                  (update route
                          :operations
                          (partial mapv
                                   #(-> %
                                        (update-in [:pt :prng] some?)
                                        (update :pt (fn [pt] (into {} pt))))))))))
   "operations with distribution in two routes")
  (is (= #::sut{:routes {:a {:route-id :a
                             :probability 0.3
                             :operations [{:pt 1}]}}
                :route-dstb {:categories {:a 0.3}
                             :total-weight 0.3}}
         (-> {::sut/routes {:a {:route-id :a
                                :probability 0.3
                                :operations [{:pt 1}]}}}
             (sut/start (opt-prng/prng {:seed #uuid "e85427c1-ed25-4ed4-9b11-52238d268265"
                                        :prng-name :xoroshiro128}))
             (update ::sut/route-dstb dissoc :prng)))
      "An operation with an integer for a pt only"))

(def prng (opt-prng/prng {}))

(deftest pick-route-id-test
  (is (= :a
         (let [prng (opt-prng/prng {})]
           (-> #::sut{:routes {:a {:operations [{:m :m4
                                                 :pt {:dstb-name :normal
                                                      :location 20
                                                      :scale 0.2}}]}
                               :b {:operations [{:m :m1
                                                 :pt {:dstb-name :normal
                                                      :location 10
                                                      :scale 0.2}}]}}
                      :route-dstb (opt-dstb/distribution #::opt-dstb{:dstb-name :categorical
                                                                     :prng prng
                                                                     :category-probabilities
                                                                     {:a 0.2
                                                                      :b 0.2}})}
               sut/pick-route-id)))
      "Pick one"))
