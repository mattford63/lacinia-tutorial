(ns user
  (:require
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.java.browse :refer [browse-url]]
   [clojure.walk :as walk]
   [clojure-game-geek.system :as system]
   [com.stuartsierra.component :as component])
  (:import (clojure.lang IPersistentMap)))

(defonce system nil)

(defn start []
  (alter-var-root #'system (fn [_]
                             (-> (system/new-system)
                                 component/start-system)))
  (browse-url "http://localhost:8888/")
  :started)

(defn stop []
  (when (some? system)
    (component/stop-system system)
    (alter-var-root #'system (constantly nil)))
  :stopped)
