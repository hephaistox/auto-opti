(ns auto-opti.eval-test
  (:require
   [auto-core.schema :as core-schema]
   [auto-opti        :as-alias opti]
   [auto-opti.eval   :as sut]
   #?(:clj [clojure.test :refer [deftest is testing]]
      :cljs [cljs.test :refer [deftest is testing] :include-macros true])))

(deftest registry-schema-test
  (is (nil? (core-schema/validate-humanize sut/registry-schema))
      "The registry-schema itself is a well-formed malli schema.")
  (testing "Names are keywords as declared by `id`."
    (is (= :keyword sut/id))
    (is (every? keyword? (keys sut/registry))))
  (is (nil? (core-schema/validate-data-humanize sut/registry-schema sut/registry))
      "The registry validates against its own closed, `::opti`-qualified schema."))

(deftest registry-content-test
  (testing "The built-in montecarlo-pi evaluation is registered with the expected entries."
    (let [entry (:montecarlo-pi sut/registry)]
      (is (some? entry) "montecarlo-pi is registered")
      (is (string? (::opti/doc entry)))
      (is (keyword? (::opti/rep-type entry)))
      (is (fn? (::opti/valid-pars entry)))
      (is (fn? (::opti/eval-fn entry))))))

(deftest registry-dispatch-test
  (testing "`:valid-pars` looked up through the registry validates the model."
    (let [valid-pars (-> sut/registry
                         :montecarlo-pi
                         ::opti/valid-pars)]
      (is (nil? (valid-pars {}
                            nil
                            {:radius 10
                             :iterations 1}))
          "Valid model returns nil")
      (is (some? (valid-pars {}
                             nil
                             {:radius "r"
                              :iterations "i"}))
          "Invalid model returns errors")))
  (testing "`:eval` looked up through the registry runs the evaluation."
    (let [eval-fn (-> sut/registry
                      :montecarlo-pi
                      ::opti/eval-fn)
          pi (eval-fn {}
                      nil
                      {:radius 100
                       :iterations 100000}
                      {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"})]
      (is (number? pi))
      (is (< 3.0 pi 3.3) "Montecarlo approximation is close to π")))
  (testing "`:eval` returns nil when iterations is not a number."
    (let [eval-fn (-> sut/registry
                      :montecarlo-pi
                      ::opti/eval-fn)]
      (is (nil? (eval-fn {}
                         nil
                         {:radius 100
                          :iterations nil}
                         {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"}))))))

(deftest eval-map-test
  (is (= {:error :not-found
          :kw :non-existing}
         (sut/eval-map :non-existing {} nil {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"}))
      "A missing one is ok")
  (is (let [eval-fn (sut/eval-map :montecarlo-pi
                                  nil
                                  nil
                                  {:radius 100
                                   :iterations 100})]
        (eval-fn {:seed #uuid "6db832f7-c10a-414a-b08b-eb5ef1d9b4fe"}))
      "A valid evaluation"))
