(ns auto-opti.routings-test
  (:require
   [auto-opti.dstb     :as opt-dstb]
   [auto-opti.prng     :as opt-prng]
   [auto-opti.routings :as sut]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(deftest machines-test
  (is (= [:m1 :m2 :m3 :m4]
         (sut/machines {:routes {:blue {:operations [{:m :m4} {:m :m2} {:m :m1}]}
                                 :purple {:operations [{:m :m4} {:m :m3} {:m :m1}]}}}))
      "Extract machines"))

(deftest start-test
  (is (= {:routes {}
          :route-dstb {:prng true
                       :categories {}
                       :total-weight 0}}
         (-> {:routes {}}
             (sut/start (opt-prng/xoroshiro128 #uuid "e85427c1-ed25-4ed4-9b11-52238d268265"))
             (update :route-dstb #(into {} %))
             (update-in [:route-dstb :prng] some?))))
  (is
   (=
    {:routes {:a {:route-id :a
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
    (-> {:routes {:a {:route-id :a
                      :probability 0.3
                      :operations [{:pt {:dstb-name :uniform
                                         :a 10
                                         :b 15}}]}
                  :b {:route-id :a
                      :probability 0.4
                      :operations [{:pt {:dstb-name :uniform
                                         :a 10
                                         :b 15}}]}}}
        (sut/start (opt-prng/xoroshiro128 #uuid "e85427c1-ed25-4ed4-9b11-52238d268265"))
        (update :route-dstb #(into {} %))
        (update-in [:route-dstb :prng] some?)
        (update :routes
                update-vals
                (fn [route]
                  (update route
                          :operations
                          (partial mapv
                                   #(-> %
                                        (update-in [:pt :prng] some?)
                                        (update :pt (fn [pt] (into {} pt))))))))))
   "operations with distribution in two routes")
  (is (= {:routes {:a {:route-id :a
                       :probability 0.3
                       :operations [{:pt 1}]}}
          :route-dstb {:prng true
                       :categories {:a 0.3}
                       :total-weight 0.3}}
         (-> {:routes {:a {:route-id :a
                           :probability 0.3
                           :operations [{:pt 1}]}}}
             (sut/start (opt-prng/xoroshiro128 #uuid "e85427c1-ed25-4ed4-9b11-52238d268265"))
             (update :route-dstb #(into {} %))
             (update-in [:route-dstb :prng] some?)))
      "An operation with an integer only"))

(def u #uuid "e85427c1-ed25-4ed4-9b11-52238d268265")

(deftest pick-route-id-test
  (is (= :b
         (sut/pick-route-id {:routes {:a {:operations [{:m :m4
                                                        :pt {:dstb-name :normal
                                                             :location 20
                                                             :scale 0.2}}]
                                          :id :a
                                          :category-probability 0.2}
                                      :b {:operations [{:m :m1
                                                        :pt {:dstb-name :normal
                                                             :location 10
                                                             :scale 0.2}}]
                                          :id :b
                                          :category-probability 0.2}}
                             :route-dstb (opt-dstb/dstb (opt-prng/xoroshiro128 u)
                                                        {:dstb-name :categorical
                                                         :category-probabilities {:a 0.5
                                                                                  :b 1}})}))
      "Pick one"))
