(ns clojure-game-geek.db
  (:require [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [com.stuartsierra.component :as component]
            [io.pedestal.log :as log]
            [clojure.string :as str])
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

(defn ^:private query
  [component statement]
  (let [[sql & params] statement]
    (log/debug :sql (str/replace sql #"\s+" " ")
               :params params)
    (jdbc/query component statement)))

(defn ^:private execute!
  [component statement]
  (let [[sql & params] statement]
    (log/debug :sql (str/replace sql #"\s+" " ")
               :params params)
    (jdbc/execute! component statement)))

;; find - primary key lookup
;; list - sequence lookup
(defn find-game-by-id
  [db game-id]
  (first
   (query db
          ["select game_id, name, summary, min_players,
            max_players, created_at, updated_at from
            board_game where game_id = ?" game-id])))

(defn find-member-by-id
  [db member-id]
  (first
    (query db
           ["select member_id, name, created_at, updated_at
             from member
             where member_id = ?" member-id])))

(defn list-games
  [db]
  (query db ["select game_id, name, summary, min_players,
            max_players, created_at, updated_at from
            board_game"]))

(defn list-designers-for-game
  [db game-id]
  (query db
         ["select d.designer_id, d.name, d.uri, d.created_at, d.updated_at
           from designer d
           inner join designer_to_game j on (d.designer_id = j.designer_id)
           where j.game_id = ?
           order by d.name" game-id]))

(defn list-games-for-designer
  [db designer-id]
  (query db
         ["select g.game_id, g.name, g.summary, g.min_players, g.max_players, g.created_at, g.updated_at
           from board_game g
           inner join designer_to_game j on (g.game_id = j.game_id)
           where j.designer_id = ?
           order by g.name" designer-id]))

(defn list-ratings-for-game
  [db game-id]
  (query db
         ["select game_id, member_id, rating, created_at, updated_at
           from game_rating
           where game_id = ?" game-id]))

(defn list-ratings-for-member
  [db member-id]
  (query db
         ["select game_id, member_id, rating, created_at, updated_at
           from game_rating
           where member_id = ?" member-id]))

(defn upsert-game-rating
  "Adds a new game rating, or changes the value of an existing game rating.

  Returns nil"
  [db game-id member-id rating]
  (execute! db
         ["insert into game_rating (game_id, member_id, rating)
           values (?, ?, ?)
           on conflict (game_id, member_id)
           do update set rating = ?"
          game-id member-id rating rating])
  nil)
