(ns data-quality.validation
  (:require [clojure.tools.logging :as log]
            [clj-time.format :as ft])
  (:require [data-quality.util :as u]))

(def email-regex #"(?i)^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,63}$")

(def title-regex #"(?i)^(mrs|mr|dr|prof)\.?|miss$")         ;;prof added

(def phone-regex #"[+0]?[1-9][0-9]{9,}")

(def postcode-regex #"(?i)[a-z]{1,2}[0-9][0-9a-z]?\s+?[0-9][a-z]{2}")

(def chipscode-regex #"(?i)[a-z0-9]{4,6}")

(def date-regex #"(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])/((19|20)[0-9][0-9])")

(defn is-decimal-gt0?
  "check if the number is decimal and if > 0, nil otherwise"
  [value]
  (try
    (let [n (bigdec value)]
      (if (and (> n 0) (decimal? n)) n nil))
    (catch Exception x
      nil
      (log/error "ERROR in format number for:" value))))

(defn is-timestamp?
  "check if date is compatible with the forma, nil otherwise"
  [value]
  (try
    (let [custom-formatter (ft/formatter "d/M/yyyy HH:mm:ss")] ;;wrong spec
      (ft/parse custom-formatter value))
    (catch Exception x
      nil
      (log/error "ERROR" (.getMessage x)))))

(defn is-url?
  "check if the url valid"
  [url]
  (:tld (u/entry-url-components url)))