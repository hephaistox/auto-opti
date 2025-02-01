(ns auto-opti.dstb.impl.category-test
  (:require
   [auto-opti.dstb.dstb-protocol :as opt-dstb-prot]
   [auto-opti.dstb.impl.category :as sut]
   [auto-opti.prng               :as opt-prng]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(def u #uuid "e3ba905f-1861-49ef-9af2-1b125df56426")

(deftest pick-category-test
  (is (and (nil? (sut/pick-category -1
                                    {:a 2
                                     :b 3}))
           (nil? (sut/pick-category 6
                                    {:a 2
                                     :b 3})))
      "If value is outside the categories range, returns nil")
  (is (= :a
         (sut/pick-category 0
                            {:a 2
                             :b 3}))
      "0 returns the first category")
  (is (= :a
         (sut/pick-category 1
                            {:a 2
                             :b 3}))
      "In the first category")
  (is (= :b
         (sut/pick-category 2
                            {:a 2
                             :b 3}))
      "Reaching the end of the first category"))

(deftest make-test
  (is (nil? (opt-dstb-prot/draw (sut/make (opt-prng/xoroshiro128 u) {})))
      "If no category could be found")
  (is (= :a (opt-dstb-prot/draw (sut/make (opt-prng/xoroshiro128 u) {:a 10})))
      "An only category is found")
  (is (= :b
         (opt-dstb-prot/draw (sut/make (opt-prng/xoroshiro128 u)
                                       {:a 0.10
                                        :b 0.20})))
      "Take one of the category")
  (is (= :a
         (opt-dstb-prot/median (sut/make (opt-prng/xoroshiro128 u)
                                         {:a 10
                                          :b 2})))
      "The median is in :a")
  (is (= :a (opt-dstb-prot/minimum (sut/make (opt-prng/xoroshiro128 u) [[:a 10] [:c 144] [:b 12]])))
      "Minimumn returns the first one")
  (is (= :b (opt-dstb-prot/maximum (sut/make (opt-prng/xoroshiro128 u) [[:a 10] [:c 144] [:b 12]])))
      "Maximumn returns the first one"))

(comment
  (time (let [d (sut/make (opt-prng/xoroshiro128 u)
                          {:a 0.10
                           :b 0.20})]
          (->> (repeat 3000000 d)
               (mapv opt-dstb-prot/draw)
               frequencies)))
  ;; Close to frequencies
)
