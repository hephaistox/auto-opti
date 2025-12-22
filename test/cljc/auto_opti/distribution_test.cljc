(ns auto-opti.distribution-test
  (:require
   [auto-core.schema       :as core-schema]
   #?@(:clj [[clojure.test :refer [deftest is]]]
       :cljs [[cljs.test :refer [deftest is] :include-macros true]])
   [auto-opti.distribution :as sut]
   [auto-opti.prng         :as opt-prng]))

(deftest distribution-test
  (is (-> (sut/distribution {})
          sut/draw
          number?)
      "Is an empty parameter map creating a valid distribution")
  (is (-> (sut/distribution #::sut{:seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"})
          sut/draw
          number?)
      "If seed is provided")
  (is (= 1.929382551206999E9
         (-> #::sut{:seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"}
             sut/distribution
             sut/draw))
      "Random")
  (is (= 27.96877525845019
         (-> #::sut{:seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"
                    :dstb-name :uniform
                    :a 10
                    :b 30}
             sut/distribution
             sut/draw))
      "Random"))

(deftest as-int-pair-test
  (is (nil? (core-schema/validate-data-humanize [:sequential int?]
                                                (opt-prng/as-int-pair (opt-prng/prng {}) 10 16)))
      "Is generated `as-int` actually an Integer."))
