(defproject clojure-game-geek "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.walmartlabs/lacinia "0.38.0"]
                 [lacinia "0.1.0-XABLAW"]
                 [clj-commons/pomegranate "1.2.0"]
                 [com.walmartlabs/lacinia-pedestal "0.15.0"]]
  :repl-options {:init-ns clojure-game-geek.core})
