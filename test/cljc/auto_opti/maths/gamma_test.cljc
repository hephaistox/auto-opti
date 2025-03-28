(ns auto-opti.maths.gamma-test
  (:require
   [auto-opti.maths       :as opt-maths]
   #?@(:cljs [[cljs.test :refer-macros [is deftest]]]
       :clj [[clojure.test :refer [is deftest]]])
   [auto-opti.maths.gamma :as sut]))

(def gamma-point-values
  [3.99121704344051E-26
   -1.01776034607733E-24
   2.4935128478894578E-23
   -5.859755192540227E-22
   1.3184449183215509E-20
   -2.834656574391336E-19
   5.8110459775022386E-18
   -1.133153965612936E-16
   2.0963348363839324E-15
   -3.66858596367188E-14
   6.053166840058601E-13
   -9.382408602090835E-12
   1.3604492473031712E-10
   -1.836606483859281E-9
   2.295758104824101E-8
   -2.640121820547716E-7
   2.772127911575102E-6
   -2.6335215159963472E-5
   2.2384932885968948E-4
   -0.0016788699664476714
   0.010912654781909864
   -0.06001960130050425
   0.2700882058522691
   -0.9453087204829419
   2.3632718012073544
   -3.544907701811032
   1.772453850905516
   0.886226925452758
   1.329340388179137
   3.3233509704478426
   11.631728396567448
   52.34277778455352
   287.88527781504433
   1871.2543057977882
   14034.407293483411
   119292.46199460901
   1133278.3889487856
   1.1899423083962249E7
   1.3684336546556586E8
   1.7105420683195732E9
   2.3092317922314236E10
   3.3483860987355646E11
   5.189998453040125E12
   8.563497447516206E13
   1.4986120533153358E15
   2.772432298633372E16
   5.4062429823350726E17
   1.1082798113786905E19
   2.3828015944641842E20
   5.361303587544415E21])

(def log-gamma-point-values
  [0.5723649429247001
   -0.1207822376352452
   0.2846828704729192
   1.2009736023470743
   2.453736570842442
   3.9578139676187165
   5.662562059857142
   7.534364236758734
   9.549267257301
   11.689333420797267
   13.940625219403763
   16.29200047656724
   18.73434751193645
   21.260076156244708
   23.862765841689086
   26.536914491115613
   29.277754515040815
   32.08111489594735
   34.94331577687682
   37.8610865089611
   40.8315009745308
   43.85192586067516
   46.91997879580878
   50.03349410501916
   53.19049452616927])

(deftest gamma-returns-correct-point-values
  []
  (is (= 1.0 (sut/gamma 1)))
  (is (= 1.0 (sut/gamma 2)))
  (is (= 2.0 (sut/gamma 3)))
  (is (= 6.0 (sut/gamma 4)))
  (is (= 24.0 (sut/gamma 5)))
  (is (= 120.0 (sut/gamma 6)))
  (is (= 720.0 (sut/gamma 7)))
  (is (= 5040.0 (sut/gamma 8)))
  (is (= (mapv sut/gamma (range -25.5 24)) gamma-point-values)))

(defn remove-equals
  [l]
  (->> l
       (mapv (fn [[idx expected]]
               (when-not (opt-maths/approx= 1E-14 (sut/log-gamma idx) expected)
                 {:idx idx
                  :expected expected
                  :actual (sut/log-gamma idx)
                  :gap (- expected (sut/log-gamma idx))
                  :abs-gap (opt-maths/abs (- expected (sut/log-gamma idx)))
                  :abs-gap-zero? (zero? (opt-maths/abs (- expected (sut/log-gamma idx))))
                  :fn (opt-maths/approx= (sut/log-gamma idx) expected 0.1)})))
       (remove nil?)))

(deftest log-gamma-returns-correct-point-values
  (is (empty? (->> (map vector (range 0.5 25) log-gamma-point-values)
                   remove-equals))))

(deftest gamma-pinv-returns-reference-value (is (= 0.02223223690217228 (sut/gamma-pinv 1E-8 4))))

(deftest gamma-pinv-result-is-ordered-by-p
  (is (< (sut/gamma-pinv 1E-11 4) (sut/gamma-pinv 1E-9 4) (sut/gamma-pinv 1E-7 4))))
