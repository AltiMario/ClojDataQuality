(ns data-quality.util
  (:require [clojurewerkz.urly.core :as u]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [clj-time.core :as t]
            [clj-time.format :as ft]
            [cheshire.core :as json]))


(defn day-interval
  "calculates the interval between 2 dates and return -1 in case of error or invalid day number"
  [booking-date stay-date]
  (try
    (let [custom-formatter (ft/formatter "d/M/yyyy")
          day-init (ft/parse custom-formatter booking-date)
          day-end (ft/parse custom-formatter stay-date)]
      (t/in-days (t/interval day-init day-end)))
    (catch Exception x
      (log/error "ERROR in date calculation between" booking-date "and" stay-date "=>" (.getMessage x))
      -1)))


(defn calc-discount
  "calculates the applied discount in %"
  [customer_paid original_price_ex_vat vat_on_original_price]
  (str (format "%.2f" (- 100 (* 100 (/ customer_paid
                                       (+ original_price_ex_vat vat_on_original_price))))) "%"))


(defn entry-url-components
  "return all the elements of a url"
  [value]
  (u/as-map
    (u/url-like
      (clojure.string/trim-newline (str "http://" value)))))


(defn postcode-info
  "return the JSON with the info about the postcode"
  [postcode]
  (try
    (json/parse-string (slurp (str "http://api.postcodes.io/postcodes/" (str/trim postcode))))
    (catch Exception x
      (log/error "ERROR" (.getMessage x)))))
(comment
  (def postcode "WS5  3QB"))


(defn from-csv-to-rows
  "from the flat file to rows"
  [path-file]
  (try
    (str/split-lines (slurp path-file))
    (catch Exception x
      (log/error "ERROR loading data file:" (.getMessage x)))))


(defn frequencies-stats
  "For a nice result display"
  [frequencies-map]
  (with-out-str
    (clojure.pprint/print-table
      (map (fn [[t c]] {:host t :count c})
           (sort-by second >
                    frequencies-map)))))