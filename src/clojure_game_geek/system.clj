(ns clojure-game-geek.system
  (:require
   [com.stuartsierra.component :as component]
   [clojure-game-geek.schema :as schema]
   [clojure-game-geek.server :as server]
   [clojure-game-geek.db :as db]))

(defn new-system
  []
  (component/system-map
   :server
   (component/using (server/new-server)
                    [:schema-provider])
   :schema-provider
   (component/using (schema/new-schema-provider)
                    [:db])
   :db (db/new-db)))
