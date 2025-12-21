(ns auto-opti.distribution.impl.category-test
  (:require
   [auto-opti.distribution.distribution-protocol :as opt-dstb-prot]
   [auto-opti.distribution.impl.category         :as sut]
   [auto-opti.prng                               :as opt-prng]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

(def uuid-stub #uuid "e3ba905f-1861-49ef-9af2-1b125df56426")

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
  (is (nil? (opt-dstb-prot/draw (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                     :seed uuid-stub})
                                          {})))
      "If no category could be found")
  (is (= :a
         (opt-dstb-prot/draw (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                  :seed uuid-stub})
                                       {:a 10})))
      "An only category is found")
  (is (= :b
         (opt-dstb-prot/draw (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                  :seed uuid-stub})
                                       {:a 0.10
                                        :b 0.20})))
      "Take one of the category")
  (is (= :a
         (opt-dstb-prot/median (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                    :seed uuid-stub})
                                         {:a 10
                                          :b 2})))
      "The median is in :a")
  (is (= :a
         (opt-dstb-prot/minimum (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                     :seed uuid-stub})
                                          [[:a 10] [:c 144] [:b 12]])))
      "Minimumn returns the first one")
  (is (= :b
         (opt-dstb-prot/maximum (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                                     :seed uuid-stub})
                                          [[:a 10] [:c 144] [:b 12]])))
      "Maximumn returns the first one"))

(def assembly-test-res
  {:a 100631
   :b 500232
   :c 399434
   :d 100172
   :e 499654
   :f 399877})

(deftest assembly-test
  (is
   (= assembly-test-res
      (let [d (sut/make (opt-prng/prng #::opt-prng{:prng-name :xoroshiro128
                                                   :seed uuid-stub})
                        {:a 0.10
                         :b 0.50
                         :c 0.4
                         :d 0.1
                         :e 0.5
                         :f 0.4})]
        (->> (repeat 2000000 d)
             (mapv opt-dstb-prot/draw)
             frequencies)))
   "The sum of weights of categories is `2.0`, so with the number of iteration which is `20000`.
In other words, each category occurence is proportional to its weight."))

(comment
  (require '[com.hypirion.clj-xchart :as c])
  (c/view (c/category-chart {"Values" (update-keys assembly-test-res name)}
                            {:title "Exponential"
                             :series-order (->> assembly-test-res
                                                keys
                                                sort
                                                (mapv name))
                             :theme :ggplot2})))
