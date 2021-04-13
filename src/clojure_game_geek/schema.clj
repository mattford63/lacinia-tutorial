(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]))

(defn resolve-game-by-id
  [games-map context args value]
  (let [{:keys [id]} args]
    (get games-map id)))

(defn resolve-board-game-designers
  [designers-map context args board-game]
  (->> board-game
       :designers
       (map designers-map)))

(defn resolve-designer-games
  [games-map context args designer]
  (let [{:keys [id]} designer]
    (->> games-map
         vals
         (filter #(-> % :designers (contains? id))))))

(defn resolver-map
  []
  (let [cgg-data (-> (io/resource "cgg-data.edn")
                     slurp
                     edn/read-string)
        games-map (->> cgg-data
                       :games
                       (reduce #(assoc %1 (:id %2) %2) {}))
        designer-map (->> cgg-data
                          :designers
                          (reduce #(assoc %1 (:id %2) %2) {}))]
    {:query/game-by-id (partial resolve-game-by-id games-map)
     :BoardGame/designers (partial resolve-board-game-designers designer-map)
     :Designer/games (partial resolve-designer-games games-map)}))

(defn load-schema
  []
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map))
      (schema/compile)))
