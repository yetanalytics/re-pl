(defproject re-pl "0.1.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.1"]
                 [replumb "0.2.4"]
                 [cljsjs/codemirror "5.24.0-1"]
                 [datascript "0.16.2"]
                 [com.cognitect/transit-cljs "0.8.239"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  :plugins [[lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]
            [lein-figwheel "0.5.14"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :profiles {:dev {:source-paths ["src/clj" "src/cljs" "dev"]
                   :dependencies [[figwheel-sidecar "0.5.14"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   }}

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "re-pl.core/mount-root"}
                        :compiler {:main re-pl.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true}}

                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main re-pl.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :simple
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}]})
