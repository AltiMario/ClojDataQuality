(ns data-quality.core
  (:require [clojure.string :as str]
            [schema.core :as s]
            [clojure.instant :refer [read-instant-date]]
            [clojure.tools.logging :as log]
            [clojure.string :as str]
            [digest :refer :all]
            [cheshire.core :as json]
            [clojure.java.io :as io])
  (:require [data-quality.validation :as v]
            [data-quality.util :as ut])
  (:gen-class))


(defn matches
  "to apply dynamically a regex to data"
  [r]
  (s/pred (fn [s] (re-matches r s))))


(def headers
  "headers of the csv file"
  [:email :title :firstname :secondname
   :mobilenumber :telephonenumber :postcode
   :entry_url :transaction_id :sku
   :product_type :booking_date
   :stay_date :customer_paid :original_price_ex_vat
   :vat_on_original_price :timestamp])


(s/defschema row-schema
  "schema validation to apply to the data"
  {:email                 (matches v/email-regex)
   :title                 (matches v/title-regex)
   :firstname             s/Str
   :secondname            s/Str
   :mobilenumber          (matches v/phone-regex)
   :telephonenumber       (matches v/phone-regex)
   :postcode              (matches v/postcode-regex)
   :entry_url             (s/pred v/is-url? 'v/is-url?)
   :transaction_id        (matches v/chipscode-regex)
   :sku                   (matches v/chipscode-regex)
   :product_type          (s/enum "HOTEL" "CARPARKING")
   :booking_date          (matches v/date-regex)
   :stay_date             (matches v/date-regex)
   :customer_paid         (s/pred v/is-decimal-gt0? 'v/is-decimal-gt0?)
   :original_price_ex_vat (s/pred v/is-decimal-gt0? 'v/is-decimal-gt0?)
   :vat_on_original_price (s/pred v/is-decimal-gt0? 'v/is-decimal-gt0?)
   :timestamp             (s/pred v/is-timestamp? 'v/is-timestamp?)
   })


(defn hash-email
  "generates the new entry hash-md5, using a specific salt and md5 to protect the email"
  [record]
  (assoc record :hash-md5 (md5 (str "engineercandidate" (:email record)))))


(defn expand-url
  "generates the new entry entry_url, with a map composed by all elements of the url"
  [record]
  (assoc record :entry_url (ut/entry-url-components (:entry_url record))))


(defn lead-time
  "generate the new entry lead-time, containing the interval between the booking time and the stay time"
  [record]
  (assoc record :lead-time (ut/day-interval (:booking_date record) (:stay_date record))))


(defn discount
  "generate the new entry discount, containing the discount applyed"
  [record]
  (assoc record :discount (ut/calc-discount (read-string (:customer_paid record))
                                            (read-string (:original_price_ex_vat record))
                                            (read-string (:vat_on_original_price record)))))


(defn lookup-postcode
  "generates the new entry postcode-ext, with a map composed by all postcode info of the external service http://api.postcodes.io/"
  [record]
  (assoc record :postcode-ext (ut/postcode-info (:postcode record))))


(defn enrich
  "runs the enrichment functions to the original row, generating an enriched json"
  [record]
  (->> record
       hash-email
       lookup-postcode
       expand-url
       discount
       lead-time))


(defn process-rows
  "validate the csv, apply the enrichment and generate the output JSON file"
  [rows-mapped output-file]
  (doseq [item rows-mapped]
    (try
      (json/generate-stream (enrich (s/validate row-schema item)) output-file)
      (catch Exception e
        (log/error "ERROR in transaction_id " (:transaction_id item) "=>" (.getMessage e))))))


(defn popular-booking-site
  "return a ordered list of the most popular booking sites"
  [json-file]
  (try
    (let [items (json/parsed-seq (clojure.java.io/reader json-file) true)]
      (frequencies (map #(:host (:entry_url %)) items)))
    (catch Exception e
      (log/error (.getMessage e)))))


(defn mapping-file
  "generates a map structure from the csv data"
  [input-file]
  (map (partial zipmap headers)
       (map #(str/split % #",") (ut/from-csv-to-rows input-file))))


(defn -main
  [input-file output-file]
  (try
    (io/delete-file output-file true)
    (process-rows (mapping-file input-file) (clojure.java.io/writer output-file))
    (println (ut/frequencies-stats (popular-booking-site output-file)))
    (catch Exception e
      (log/error (.getMessage e)))))

(comment
  (-main "resources/test-data.csv" "out.json")
  )