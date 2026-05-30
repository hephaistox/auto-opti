(ns auto-opti.prng.impl.xoro-common-test
  (:require
   [auto-opti.prng.impl.xoro-common :as sut]
   [clojure.test                    :refer [deftest is testing]]))

(deftest test-rotl
  (testing "rotl with known values"
    (is (= 0x2 (sut/rotl 0x1 1)) "rotl of 1 by 1 should give 2")
    (is (= 0xFF00 (sut/rotl 0xFF 8)) "rotl of 0xFF by 8 should give 0xFF00")
    (let [x 12345]
      (is (= x (sut/rotl x 0)) "rotl by 0 should return original value")
      (is (= x (sut/rotl x 64)) "rotl by 64 should return original value"))
    (is (= 0x1234560 (sut/rotl 0x123456 4)) "rotl should shift bits left with wrap")))

(deftest test-remainder-unsigned
  (testing "remainder-unsigned with known values"
    (is (= 2 (sut/remainder-unsigned 100 7)) "100 % 7 should be 2")
    (is (= 456912 (sut/remainder-unsigned 123456789 999999)) "123456789 % 999999 should be 456795")
    (is (= 57 (sut/remainder-unsigned 12345 256)) "12345 % 256 should be 57")
    (is (= 0 (sut/remainder-unsigned 100 10)) "Exact division should give 0")
    (is (= 5 (sut/remainder-unsigned -1 10)) "Unsigned -1 % 10 should be 5")))
