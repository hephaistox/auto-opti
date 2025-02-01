(ns auto-opti.prng.stateless-test
  (:require
   #?(:clj [clojure.test :refer [is deftest]]
      :cljs [cljs.test :refer [is deftest] :include-macros true])
   [auto-opti.prng.stateless :as sut]))

(defn test-jump [stateless-prng] (is (sut/jump stateless-prng) "Jump executes without errors."))

(defn test-seed
  [stateless-prng]
  (is (sut/uuid-seed stateless-prng) "Returns the seed without errors."))

(defn is-stateless-test
  [stateless-prng1]
  (let [stateless-prng2 stateless-prng1
        v1 (sut/peek-int stateless-prng1 0 10)
        _ (-> stateless-prng1
              sut/next
              sut/next
              sut/next)
        v2 (sut/peek-int stateless-prng2 0 10)]
    (is (= v1 v2) "The `prng` does not remember states.")))

(defn test-all
  "Assemble all tests. Returns the collection of returns of all tests.
  Note that if the call is wrapped in deftest, the report will be built."
  [stateless-prng]
  ((juxt test-jump test-seed is-stateless-test) stateless-prng))

(defrecord PRNGStatelessSTUB [vals]
  sut/PRNGStateless
    (jump [_]
      (PRNGStatelessSTUB. (-> vals
                              rest
                              rest
                              rest)))
    (uuid-seed [_] 1234)
    (peek-int [_ _ _] (first vals))
    (next [_] (PRNGStatelessSTUB. (rest vals))))

(deftest prngstateless-test (is (test-all (->PRNGStatelessSTUB [10 12 11 19 15])) "Test stub"))
