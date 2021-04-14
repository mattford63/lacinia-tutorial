(defproject clojure-game-geek "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-commons/pomegranate "1.2.0"]
                 [com.walmartlabs/lacinia-pedestal "0.15.0"]
                 [io.aviso/logging "0.3.2"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.2.19"]
                 [com.mchange/c3p0 "0.9.5.5"]]
  :repl-options {:init-ns clojure-game-geek.core})
