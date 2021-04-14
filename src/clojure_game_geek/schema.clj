(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]))

(defn resolve-element-by-id
  [element-map context args value]
  (let [{:keys [id]} args]
    (get element-map id)))

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

(defn resolve-member-ratings
  [ratings-map context args member]
  (let [{:keys [id]} member]
    (get ratings-map id)))

(defn entity-map
  [data k]
  (reduce #(assoc %1 (:id %2) %2) {} (get data k)))

(defn member-ratings
  [ratings-map]
  (fn [_ _ member]
    (let [id (:id member)]
      (filter #(= id (:member_id %)) ratings-map))))

(defn game-rating->game
  [games-map]
  (fn [_ _ game-rating]
    (get games-map (:game_id game-rating))))

(defn rating-summary
  [cgg-data]
  (fn [_ _ board-game]
    (let [id (:id board-game)
          ratings (->> cgg-data
                       :ratings
                       (filter #(= id (:game_id %)))
                       (map :rating))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (apply + ratings)
                     (float n)))})))

(defn resolver-map
  [component]
  (let [cgg-data (-> (io/resource "cgg-data.edn")
                     slurp
                     edn/read-string)
        games-map (entity-map cgg-data :games)
        designer-map (entity-map cgg-data :designers)
        member-map (entity-map cgg-data :members)
        ratings-map (:ratings cgg-data)]
    {:query/game-by-id (partial resolve-element-by-id games-map)
     :query/member-by-id (partial resolve-element-by-id member-map)
     :BoardGame/designers (partial resolve-board-game-designers designer-map)
     :BoardGame/rating-summary (rating-summary cgg-data)
     :Designer/games (partial resolve-designer-games games-map)
     :Member/ratings (member-ratings ratings-map)
     :GameRating/game (game-rating->game games-map)}))

(defn load-schema
  [component]
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map component))
      (schema/compile)))


(defrecord SchemaProvider [schema]

  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider []
  {:schema-provider (map->SchemaProvider {})})
