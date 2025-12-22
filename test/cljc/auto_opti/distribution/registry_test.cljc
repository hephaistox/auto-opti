(ns auto-opti.distribution.registry-test
  (:require
   [auto-core.schema                             :as core-schema]
   #?@(:clj [[clojure.test :refer [deftest is]]]
       :cljs [[cljs.test :refer [deftest is] :include-macros true]])
   [auto-opti                                    :as-alias opti]
   [auto-opti.distribution.distribution-protocol :as opt-dstb-prot]
   [auto-opti.distribution.registry              :as sut]
   [auto-opti.prng                               :as opt-prng]))

(deftest registry-test
  (is (nil? (core-schema/validate-humanize (sut/schema))))
  (is (nil? (core-schema/validate-data-humanize (sut/schema) sut/registry)))
  (is (= 20
         (-> ((-> sut/registry
                  :uniform-int)
              (opt-prng/prng {})
              #::opti{:a 13
                      :b 24})
             opt-dstb-prot/draw))
      "Uniform int check"))

(comment
  ;;To generate the list of possible parameters for the documentation.
  (->> sut/registry
       keys
       (map #(str "* `" % "`\n"))
       (apply println))
  ;
)
