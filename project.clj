(defproject trotter "0.1.0-SNAPSHOT"
  :description "An otter going places"
  :url "https://github.com/lvh/trotter"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.palletops/pallet "0.8.0-RC.9"]
                 [com.palletops/pallet-jclouds "1.7.3"]
                 [org.apache.jclouds.provider/rackspace-cloudservers-us "1.7.3"]
                 [org.apache.jclouds.driver/jclouds-jsch "1.7.3"]
                 [org.apache.jclouds.driver/jclouds-slf4j "1.7.3"]
                 [org.clojure/data.json "0.2.2"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.2.0-beta2"]
                 [ring/ring-jetty-adapter "1.2.0-beta2"]
                 [ring/ring-json "0.2.0"]
                 [ch.qos.logback/logback-classic "1.1.1"]
                 [org.slf4j/slf4j-api "1.6.4"]
                 [com.palletops/upstart-crate "0.8.0-alpha.2"]]
  :main trotter.core
  :profiles {:dev {:plugins [[com.palletops/pallet-lein "0.8.0-alpha.1"]]}})
