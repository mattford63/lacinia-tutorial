(ns clojure-game-geek.test-utils
  (:require  [clojure.walk :as walk])
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
