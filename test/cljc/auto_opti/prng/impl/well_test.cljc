(ns auto-opti.prng.impl.well-test
  (:require
   [auto-opti.prng.impl.well :as sut]
   [auto-opti.prng.stateful  :as opt-prng-stateful]
   #?(:clj [clojure.test :refer [deftest is]]
      :cljs [cljs.test :refer [deftest is] :include-macros true])))

;; (deftest well-rng-1024a-test
;;   (is (= [0.11015778360888362 0.006186490412801504 0.5537592866457999 0.8863185755908489]
;;          (take 4
;;                (-> #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3b"
;;                    sut/well-rng-1024a)))))

(deftest well-make
  (is (= 7
         (-> #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3c"
             sut/make
             (opt-prng-stateful/rnd-int 0 11)))
      "rnd-int is an int")
  (is (= 7
         (let [prng (sut/make #uuid "54b9758a-906f-4ec9-b1eb-1efef7f67e3c")]
           (opt-prng-stateful/rnd-int prng 0 11)
           (opt-prng-stateful/rnd-int prng 0 11)
           (opt-prng-stateful/rnd-int prng 0 11)))
      "rnd-int is an int"))
