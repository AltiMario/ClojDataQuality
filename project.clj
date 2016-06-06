(defproject data-quality "0.1.0-SNAPSHOT"
  :description "Validate and enrich a csv file"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [prismatic/schema "1.1.1"]
                 [clojurewerkz/urly "1.0.0"]
                 [digest "1.4.4"]
                 [log4j/log4j "1.2.17"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot data-quality.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all :uberjar-name "data-quality-standalone.jar"}
             :dev     {:dependencies [[midje "1.8.2"]]
                       :plugins      [[lein-midje "3.2"]]}})
