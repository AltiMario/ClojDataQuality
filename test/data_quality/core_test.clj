(ns data-quality.core-test
  (:require [data-quality.core :refer :all]
            [data-quality.util :as ut]
            [data-quality.validation :as v])
  (:use midje.sweet))

(facts "about `entry-url-components`"
       (fact "should return the splitted elements of a url"
             (ut/entry-url-components "http://pirmasenser-zeitung.de:900/content/abonnement/pz_card/index.shtml?var=33&cc=8#ckdk")
                  => {:protocol "http", :host "pirmasenser-zeitung.de", :port 900, :user-info nil, :path "/content/abonnement/pz_card/index.shtml", :query "var=33&cc=8", :fragment "ckdk", :tld "de"}
             (ut/entry-url-components "www.google.com") => {:protocol "http", :host "www.google.com", :port -1, :user-info nil, :path "/", :query nil, :fragment nil, :tld "com"}
             (ut/entry-url-components "https://uk.linkedin.com/in/altimario") => {:protocol "https", :host "uk.linkedin.com", :port -1, :user-info nil, :path "/in/altimario", :query nil, :fragment nil, :tld "com"}))

(facts "about `email-regex`"
       (fact "should return the email in case of well format otherwise nil"
             (re-matches v/email-regex "altimario@gmail.com")=> "altimario@gmail.com"
             (re-matches v/email-regex "altimariogmail.")=> nil))

(facts "about `title-regex`"
       (fact "should validate the titles or nil otherwise"
             (re-matches v/title-regex "MR") => ["MR" "MR"]
             (re-matches v/title-regex "mrs.") => ["mrs." "mrs"]
             (re-matches v/title-regex "test") => nil))

(facts "about `phone-regex`"
       (fact "should validate the phone number or return nil otherwise"
             (re-matches v/phone-regex "0417224511878") => "0417224511878"
             (re-matches v/phone-regex "+7224511878") => "+7224511878"
             (re-matches v/title-regex "test") => nil))

(facts "about `postcode-regex`"
       (fact "should validate the postcode nil otherwise"
             (re-matches v/postcode-regex "CT21 4JF") => "CT21 4JF"
             (re-matches v/postcode-regex "test") => nil))

(facts "about `chipscode-regex`"
       (fact "should validate the chipscode or an alphanumeric of 5/6 chars or return nil otherwise"
             (re-matches v/chipscode-regex "SOPJIR") => "SOPJIR"
             (re-matches v/chipscode-regex "SOPJI") => "SOPJI"
             (re-matches v/chipscode-regex "LTZ8") => "LTZ8"  ;;it's out of the specification
             (re-matches v/chipscode-regex "LBX") => nil))

(facts "about `date-regex`"
       (fact "should validate the date or return nil otherwise"
             (re-matches v/date-regex "15/04/2016") => ["15/04/2016" "15" "04" "2016" "20"]
             (re-matches v/date-regex "test") => nil))

(facts "about `is-decimal-gt0?`"
       (fact "should validate if it's a decimal or return nil otherwise"
             (v/is-decimal-gt0? "12.9") => 12.9M
             (v/is-decimal-gt0? "0") => nil))

(facts "about `is-timestamp?`"
       (fact "should validate the right timestamp or return nil otherwise"
             ;(v/is-timestamp? "8/4/2016 10:57:47") => "8/4/2016 10:57:47"
             (v/is-timestamp? "test") => nil
             (v/is-timestamp? "2016-157T12:25:40.501Z") => nil))

(facts "about `is-url?`"
       (fact "should validate the url, return the tld value or nil if malformed"
             (v/is-url? "www.legoland.co.uk/Plan/Tickets") => "co.uk"
             (v/is-url? "www.mario") => nil))

(facts "about `day-interval`"
       (fact "should calculate the interval between 2 dates or -1 if impossible"
             (ut/day-interval "19/03/1931" "5/06/2016") => 31125
             (ut/day-interval "1/2/1931" "05/6/2016") => 31171
             (ut/day-interval "1/2/1931" "test") => -1
             (ut/day-interval "5/7/2016" "05/6/2016") => -1))

(facts "about `calc-discount`"
       (fact "should calculate the discount"
             ;(ut/calc-discount 90 80 20) => "x%"
             (ut/calc-discount 279.27 230.32 57.58) => "3.00%"))

(facts "about `entry-url-components`"
       (fact "should return the components of a url"
             (ut/entry-url-components "app.airport-parking.uk.com/")
                  => {:protocol "http", :host "app.airport-parking.uk.com", :port -1, :user-info nil, :path "/", :query nil, :fragment nil, :tld "uk.com"}))