(ns auto-opti.sample
  "Sampling is here to manage set of measures."
  (:require
   [auto-opti.sample.impl.vector :as opt-sample-vector]
   [com.hypirion.clj-xchart      :as cfc]))

(defn make-vector [v] (opt-sample-vector/make v))

(defn histogram
  "Create a file called `filename` containing an histogram with `sample-data`."
  [sample-data
   filename
   {:keys [format title theme]
    :or {format :jpg
         theme :xchart
         title "Distribution"}
    :as _options}]
  (-> {"Sampling" sample-data}
      (cfc/category-chart {:title title
                           :theme theme
                           :x-axis {:x-order (-> sample-data
                                                 keys
                                                 sort)}})
      (cfc/spit filename format)))

(defn sampling-rounded
  "For each multiple of `interval`, returns a map turning this multiple to its number of occurences"
  [vals interval]
  (->> vals
       (map #(* interval (int (/ % interval))))
       frequencies))

(defn sampling-raw
  "For each value of `interval`, returns a map turning this multiple to its number of occurences"
  [vals]
  (->> vals
       (map str)
       frequencies))
