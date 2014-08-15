(ns trotter.core
  (:gen-class)
  (:require [trotter.web :as web]
            [clojure.java.browse :refer [browse-url]]))

(defn -main
  [& args]
  (println "trotter!!!")
  (web/run)
  (browse-url web/url))
