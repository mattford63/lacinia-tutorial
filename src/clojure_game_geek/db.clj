(ns clojure-game-geek.db
  (:require [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component])
  (:import (com.mchange.v2.c3p0 ComboPooledDataSource)))

(defn ^:private pooled-data-source
  [host dbname user password port]
  (doto (ComboPooledDataSource.)
    (.setDriverClass "org.postgresql.Driver")
    (.setJdbcUrl (str "jdbc:postgresql://" host ":" port "/" dbname))
    (.setUser user)
    (.setPassword password)))

(defrecord ClojureGameGeekDb [datasource]

  component/Lifecycle

  (start [this]
    (assoc this :datasource (pooled-data-source "localhost" "cggdb"
                                                 "cgg_role" "lacinia" 25432)))

  (stop [this]
    (-> datasource .close)
    (assoc this :datasource nil)))

(defn new-db
  []
  (map->ClojureGameGeekDb {}))

;; find - primary key lookup
;; list - sequence lookup
(defn find-game-by-id
  [db game-id]
  (first
   (jdbc/query db
               ["select game_id, name, summary, min_players, max_players, created_at, updated_at from board_game where game_id = ?" game-id])))

(defn find-member-by-id
  [db member-id]
  (->> db
       :data
       deref
       :members
       (filter #(= member-id (:id %)))
       first))

(defn list-games
  [db]
  (->> db
       :data
       deref
       :games))

(defn list-designers-for-game
  [db game-id]
  (let [designers (:designers (find-game-by-id db game-id))]
    (->> db
         :data
         deref
         :designers
         (filter #(contains? designers (:id %))))))

(defn list-games-for-designer
  [db designer-id]
  (->> db
       :data
       deref
       :games
       (filter #(contains? (:designers %) designer-id))))

(defn list-ratings-for-games
  [db game-id]
  (->> db
       :data
       deref
       :ratings
       (filter #(= game-id (:game_id %)))))

(defn list-ratings-for-member
  [db member-id]
  (->> db
       :data
       deref
       :ratings
       (filter #(= member-id (:member_id %)))))

(defn ^:private apply-game-rating
  [game-ratings game-id member-id rating]
  (->> game-ratings
       (remove #(and (= game-id (:game_id %))
                     (= member-id (:member_id %))))
       (cons {:game_id game-id
              :member_id member-id
              :rating rating})))

(defn upsert-game-rating
  [db game-id member-id rating]
  (-> db
      :data
      (swap! update :ratings apply-game-rating game-id member-id rating)))
