(ns user
  (:require
   [clojure-game-geek.schema :as s]
   [com.walmartlabs.lacinia :as lacinia]
   [clojure.walk :as walk])
  (:import (clojure.lang IPersistentMap)))

(def schema (s/load-schema))

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

(defn q [query-string]
  (simplify (lacinia/execute schema query-string nil nil)))

(comment
  (q "{game_by_id(id:  \"1237\") {name summary}}"))
