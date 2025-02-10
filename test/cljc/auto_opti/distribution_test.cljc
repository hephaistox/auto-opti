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
  (is (-> (sut/distribution {:seed #uuid "31bf8660-b31b-4c1c-b440-8ecf82e0a477"})
          sut/draw
          number?)
      "If seed is provided"))

(deftest as-int-pair-test
  (is (nil? (core-schema/validate-data-humanize
             [:sequential int?]
             (opt-prng/as-int-pair (opt-prng/xoroshiro128) 10 16)))
      "Is generated `as-int` actually an Integer."))
