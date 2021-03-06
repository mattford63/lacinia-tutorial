(ns clojure-game-geek.schema
  (:require [clojure.java.io :as io]
            [com.walmartlabs.lacinia.util :as util]
            [com.walmartlabs.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [clojure.edn :as edn]
            [com.stuartsierra.component :as component]
            [clojure-game-geek.db :as db]))

(defn games [db]
  (fn [_ _ _]
    (db/list-games db)))

(defn game-by-id [db]
  (fn [_ args _]
    (db/find-game-by-id db (:id args))))

(defn member-by-id [db]
  (fn [_ args _]
    (db/find-member-by-id db (:id args))))

(defn board-game-designers [db]
  (fn [_ _ board-game]
    (db/list-designers-for-game db (:game_id board-game))))

(defn designer-games [db]
  (fn [_ _ designer]
    (db/list-games-for-designer db (:designer_id designer))))

(defn rating-summary
  [db]
  (fn [_ _ board-game]
    (let [ratings (map :rating (db/list-ratings-for-game db (:game_id board-game)))
          n (count ratings)]
      {:count n
       :average (if (zero? n)
                  0
                  (/ (apply + ratings)
                     (float n)))})))

(defn member-ratings
  [db]
  (fn [_ _ member]
    (db/list-ratings-for-member db (:member_id member))))

(defn game-rating->game
  [db]
  (fn [_ _ game-rating]
    (db/find-game-by-id db (:game_id game-rating))))

(defn rate-game
  [db]
  (fn [_ args _]
    (let [{game-id :game_id
           member-id :member_id
           rating :rating} args
          game (db/find-game-by-id db game-id)
          member (db/find-member-by-id db member-id)]
      (cond
        (nil? game)
        (resolve-as nil {:message "Game not found."
                         :status 404})

        (nil? member)
        (resolve-as nil {:message "Member not found."
                         :status 404})

        (not (<= 1 rating 5))
        (resolve-as nil {:message "Rating must be between 1 and 5"
                         :status 400})

        :else
        (do
          (db/upsert-game-rating db game-id member-id rating)
          game)))))

(defn resolver-map
  [db]
  {:query/game-by-id (game-by-id db)
   :query/member-by-id (member-by-id db)
   :query/games (games db)
   :BoardGame/designers (board-game-designers db)
   :BoardGame/rating-summary (rating-summary db)
   :Designer/games (designer-games db)
   :Member/ratings (member-ratings db)
   :GameRating/game (game-rating->game db)
   :mutation/rate-game (rate-game db)})

(defn load-schema
  [db]
  (-> (io/resource "cgg-schema.edn")
      slurp
      edn/read-string
      (util/attach-resolvers (resolver-map db))
      (schema/compile)))


(defrecord SchemaProvider [db schema]

  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema db)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider []
  (map->SchemaProvider {}))
