(ns auto-opti.prng.stateful-test
  (:require
   #?@(:clj [[clojure.test :refer [is testing deftest]]]
       :cljs [[cljs.test :refer [is testing deftest] :include-macros true]])
   [auto-core.schema                 :as core-schema]
   [auto-opti.prng.impl.xoroshiro128 :as opt-prng-xoro]
   [auto-opti.prng.stateful          :as sut]))

(defn test-duplicate
  [stateful-prng]
  (let [first-rnd (sut/rnd-int stateful-prng 10 100)
        prng-dup (sut/duplicate stateful-prng)]
    (is (= first-rnd (sut/rnd-int prng-dup 10 100))
        "Is duplicate creates a prng starting again at the seed.")))

(defn test-jump
  [stateful-prng]
  (testing "Jump executes without errors." (is (sut/jump stateful-prng))))

(defn test-seed
  [stateful-prng]
  (testing "Returns the seed without errors." (is (sut/uuid-seed stateful-prng))))

(defn test-reset
  [stateful-prng]
  (testing "Reset comes back to the seed so rnd returns same values."
    (sut/reset stateful-prng)
    (let [first-val (sut/rnd-int stateful-prng 10 100)
          _ (sut/reset stateful-prng)
          second-attempt (sut/rnd-int stateful-prng 10 100)]
      (is (= first-val second-attempt)))))

(defn is-stateful-test
  [stateful-prng]
  (testing "Are rnd differents?"
    (is (not= (sut/rnd-int stateful-prng 0 100)
              (sut/rnd-int stateful-prng 0 100)
              (sut/rnd-int stateful-prng 0 100)))))

(defn test-all
  [stateful-prng]
  ((juxt test-duplicate is-stateful-test test-jump test-seed test-reset) stateful-prng))

(defn test-non-repeatable [stateful-prng] ((juxt is-stateful-test test-jump) stateful-prng))

(deftest as-ints-test
  (testing "Are integer drawn in the range."
    (is (nil? (core-schema/validate-data-humanize [:sequential int?]
                                                  (sut/as-ints (opt-prng-xoro/make) 10 0 100))))))
