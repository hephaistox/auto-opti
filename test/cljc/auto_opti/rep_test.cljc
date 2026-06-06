(ns auto-opti.rep-test
  (:require
   [auto-core.schema :as core-schema]
   [auto-opti.rep    :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(def a-uuid #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b")

(deftest registry-schema-test
  (is (nil? (core-schema/validate-humanize sut/registry-schema))
      "The registry-schema is a well-formed malli schema.")
  (is (nil? (core-schema/validate-data-humanize sut/registry-schema sut/registry))
      "The registry validates against its own schema.")
  (is (every? keyword? (keys sut/registry)) "Rep-type names are keywords."))

(deftest valid-rep-seed-test
  (testing "a well-formed :seed representation is valid"
    (is (nil? (sut/valid-rep :seed {:seed a-uuid}))))
  (testing "a representation missing :seed or with a non-uuid :seed is rejected"
    (is (some? (sut/valid-rep :seed {})) "missing :seed")
    (is (some? (sut/valid-rep :seed {:seed "not-a-uuid"})) ":seed is a string")
    (is (some? (sut/valid-rep :seed {:seed 42})) ":seed is a number")
    (is (contains? (sut/valid-rep :seed {}) :rep) "errors are returned under :rep"))
  (testing "an unknown rep-type reports :not-found"
    (let [res (sut/valid-rep :does-not-exist {:seed a-uuid})]
      (is (= :not-found (:error res)))
      (is (= :does-not-exist (:rep-type res)))))
  (testing "an explicit registry can be passed"
    (is (nil? (sut/valid-rep {:seed [:map [:seed :uuid]]} :seed {:seed a-uuid})))))
