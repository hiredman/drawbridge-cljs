(defproject drawbridge-cljs "0.0.2-SNAPSHOT"
  :description "drawbridge client for clojurescript"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild {:builds [
               {:id :main
                :source-paths ["src"]
                :compiler {:output-to "target/foo.js"
                           :optimizations :whitespace
                           :pretty-print true}
                :jar true}]})
