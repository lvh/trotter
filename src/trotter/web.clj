(ns trotter.web
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response header redirect]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :refer [wrap-json-response
                                          wrap-json-body]]
            [compojure.core :refer [GET POST DELETE PUT routes]]
            [compojure.handler :refer [api]]
            [pallet.compute :as compute]
            [pallet.node :as node]
            [trotter.converger :refer [converge-to-config service starting-configuration]]))

(defn format-phase-details
  "Format the details for a single convergence phase."
  [result]
  (let [succeeded? (fn [map] (= (get map :exit 0) 0))
        failed? (complement succeeded?)]
    {:total (count result)
     :failed (count (filter failed? result))
     :success (count (filter succeeded? result))
     :results (for [r result]
                (select-keys r [:exit :out :context :action-symbol]))}))

(defn format-convergence-details
  "Given a result, format the details of the result.

  Specifically, display the details for each phase, and display if all
  phases (together) succeeded."
  [res]
  (let [phases (for [{:keys [phase result target]} (:results res)
                     :let [details (format-phase-details result)]]
                 {:phase phase
                  :hostname (-> target :node node/hostname)
                  :details details
                  :success (= (:total details) (:success details))})]
    {:phases (vec phases)
     :success (every? :success phases)}))

(defn ^:private clean-up
  "Clean up a desired configuration, by removing any bogus junk the
  evil client may have thrown in there, or brain damage added by the
  Javascript app or the JSON parser. Additionally, converts roles and
  group names to :keywords.
  "
  [desired-configuration]
  (->>
   (for [[group-name {:keys [size roles]}] desired-configuration]
     [(keyword group-name)
      {:size (if (string? size) (Integer/parseInt size) size)
       :roles (map keyword roles)}])
   (reduce merge {})))

(defn ^:private router
  "Route requests to the appropriate places."
  []
  (routes
   (GET "/"
        request
        (redirect "/index.html"))
   (GET "/api/nodes"
        request
        (response
         (->> (compute/nodes @service)
              (map node/node-map))))
   (GET "/api/starting-configuration"
        request
        (response starting-configuration))
   (PUT "/api/configuration"
        {{:keys [desired phases]} :body}
        (response (-> (converge-to-config
                       (clean-up desired)
                       (map keyword phases))
                      (format-convergence-details))))))

(def app
  (-> (api (router))
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-resource "assets")))

(def host "127.0.0.1")
(def port 8080)
(def url (str "http://" host ":" port "/"))

(defn run
  []
  (run-jetty app {:host host :port port}))
