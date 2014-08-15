(ns trotter.converger
  (:require [pallet.configure :as configure]
            [pallet.api :as api]
            [trotter.crate.redis :as redis]
            [trotter.crate.nginx :as nginx]
            [trotter.crate.shorten :as shorten]))

(def node-spec
  "The network, hardware and image specification for a single node.

  If this looks incredibly similar to the stuff you pass to a Nova
  call, that's because it is (through jclouds); although pallet
  supports many more backends, both directly and through jclouds.

  Note that these are currently the same for all nodes. Notably they
  have inbound-ports configured as the union of all required ports
  over all roles. This should probably be part of the thing we
  currently call launchConfiguration."
  (api/node-spec
   :network  {:inbound-ports [22 80 6379 8080]}
   :image {:os-family :ubuntu
           :os-version-matches "12.04"}
   :hardware {:min-cores 1
              :min-disk 10
              :min-ram 512}))

(def role-specs
  "Some customizations for the specific roles.

  Note how in pallet, it's really easy to customize an existing
  crate (nginx, redis...) with *only* the customizations that you
  actually want."
  {:nginx #(nginx/server-spec
              {:vhosts {:shorten {:role :shorten
                                  :port 80
                                  :upstream :shorten}}
               :upstreams {:shorten {:role :shorten
                                     :port 8080}}})
   :redis   #(redis/server-spec {:bind "0.0.0.0"})
   :shorten #(shorten/server-spec {:listen_host "0.0.0.0"})})

(def starting-configuration
  "A suggested starting configuration."
  {:load-balancer {:roles [:nginx]
                   :size 1}
   :database {:roles [:redis]
              :size 1}
   :web-worker {:roles [:shorten]
                :size 3}})

(defn get-rackspace-service
  "Get a configured service named :rackspace. Hopefully it will
  contain some credentials to enter the Rackspace cloud.

  This can take a while. You probably want to cache the result."
  []
  (let [credentials (get-in
                      (configure/pallet-config)
                      [:services :rackspace])]
     (configure/compute-service-from-map credentials)))

(def service
  "A (global) reference to the current service.

  This is wrapped in delay, because getting the service involves a
  bunch of IO, and can therefore take a while."
  (delay (get-rackspace-service)))

(defn ^:private desired-count-by-group
  "Turn a desired configuration (see start-desired-configuration for
  an example) into a mapping of groups to the desired capacity for
  each role."
  [desired-config]
  (->> (for [[group-name {:keys [size roles] :or {size 1}}] desired-config]
         [(api/group-spec
           group-name
           :extends (for [role roles] ((get role-specs (keyword role))))
           :node-spec node-spec)
          size])
       (reduce merge {})))

(defn converge-to-config
  "Given a desired configuration, make it so."
  [desired-config phases]
  (let [counts (desired-count-by-group desired-config)]
    (api/converge counts :compute @service :phase phases)))
