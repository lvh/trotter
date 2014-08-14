(defproject trotter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.palletops/pallet "0.8.0-RC.9"]
                 [com.palletops/pallet-jclouds "1.7.3"]
                 [org.apache.jclouds.provider/rackspace-cloudservers-us "1.8.0"]
                 [compojure "1.1.8"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [ring/ring-json "0.3.1"]]
  :profiles {:dev {:plugins [[com.palletops/pallet-lein "0.8.0-alpha.1"]]}})
