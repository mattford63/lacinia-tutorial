(ns user
  (:require
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.java.browse :refer [browse-url]]
   [clojure.walk :as walk]
   [clojure-game-geek.system :as system]
   [com.stuartsierra.component :as component])
  (:import (clojure.lang IPersistentMap)))

(defn simplify [m]
  (walk/postwalk
   (fn [node]
     (cond
       (instance? IPersistentMap node)
       (into {} node)

       (seq? node)
       (vec node)

       :else
       node))
   m))

(defonce system (system/new-system))

(defn start []
  (alter-var-root #'system component/start-system)
  (browse-url "http://localhost:8888/")
  :started)

(defn stop []
  (alter-var-root #'system component/stop-system)
  :stopped)
