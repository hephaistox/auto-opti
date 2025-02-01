(ns auto-opti.time-based.impl.aggregate-test
  (:require
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])
   [auto-core.schema                    :as core-schema]
   [auto-opti.time-based                :as opt-tb]
   [auto-opti.time-based.impl.aggregate :as sut]))

(deftest schema-test (is (nil? (core-schema/validate-humanize sut/schema)) "valid schema"))

(deftest validate-test
  (is (nil? (core-schema/validate-data-humanize sut/schema #::opt-tb{:start-bucket 0}))))

(deftest set-end-bucket-test
  (is (= #::opt-tb{:start-bucket 1
                   :end-bucket 10}
         (sut/set-end-bucket #::opt-tb{:start-bucket 1} #::opt-tb{:start-bucket 10}))
      "next aggregate with a `start-bucket` is updating `aggregate` `end-bucket`")
  (is
   (= #::opt-tb{:start-bucket 1} (sut/set-end-bucket #::opt-tb{:start-bucket 1} {}))
   "If the value `start-bucket` in the next `aggregate` is not defined, the `end-bucket` of the `aggregate` is set to its `start-bucket`."))
