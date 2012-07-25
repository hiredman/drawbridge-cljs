(defproject drawbridge-cljs "1.0.0-SNAPSHOT"
  :description "drawbridge client for clojurescript"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :dev-dependencies [[lein-cljsbuild "0.2.4"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds
              {:main {:source-path "src-cljs/"
                      :compiler {:output-to "foo.js"
                                 :optimizations :whitespace
                                 :pretty-print true}
                      :jar true}}})
