(defproject minimal-chat "0.1.0-SNAPSHOT"
  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]

                 [reagent "0.5.1"]
                 [reagent-forms "0.5.12"]
                 [reagent-utils "0.1.5"]
                 [secretary "1.2.3"]
                 [cljsjs/firebase "2.2.7-1"]
                 [matchbox "0.0.8-SNAPSHOT"]

                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [selmer "0.9.2"]
                 [environ "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-environ "1.0.1"]
            [lein-ring "0.9.7"]]

  :ring {:handler minimal-chat.handler/app
         :uberwar-name "minimal-chat.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "minimal-chat.jar"

  :main minimal-chat.server

  :hooks [leiningen.cljsbuild]

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        ;;:externs       ["react/externs/react.js"]
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns minimal-chat.handler
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.4.0"]
                                  [leiningen "2.5.3"]
                                  [figwheel "0.4.0"]
                                  [weasel "0.7.0"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [pjstadig/humane-test-output "0.7.0"]
                                  [com.cemerick/double-check "0.6.1"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.4.0"]
                             [com.cemerick/clojurescript.test "0.3.3"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 54424
                              :css-dirs ["resources/public/css"]
                              :ring-handler minimal-chat.handler/app}

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "minimal-chat.dev"
                                                         :source-map true}}
                                        :test {:source-paths ["src/cljs"  "test/cljs"]
                                               :notify-command ["phantomjs"
                                                                :cljs.test/runner
                                                                "test/vendor/es5-shim.js"
                                                                "test/vendor/es5-sham.js"
                                                                "test/vendor/console-polyfill.js"
                                                                "target/test.js"]
                                               :compiler {:output-to "target/test.js"
                                                          :optimizations :whitespace
                                                          :pretty-print true
                                                          :preamble ["react/react.js"]}}
                                        :test2 {:source-paths ["src/cljs"  "test/cljs"]
                                               :notify-command ["phantomjs"
                                                                :cljs.test/runner
                                                                "test/vendor/es5-shim.js"
                                                                "test/vendor/es5-sham.js"
                                                                "test/vendor/console-polyfill.js"
                                                                "target/test.js"
                                                                ]
                                               :compiler {:output-to "target/test.js"
                                                          :optimizations :whitespace
                                                          :pretty-print true
                                                          :preamble ["react/react.js"]}}}
                               :test-commands {"unit" ["phantomjs" :runner
                                                       "test/vendor/es5-shim.js"
                                                       "test/vendor/es5-sham.js"
                                                       "test/vendor/console-polyfill.js"
                                                       "target/test.js"]}}}

             :uberjar {:env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :whitespace
                                              :pretty-print false}}}}}

             :production {:ring {:open-browser? false
                                 :stacktraces?  false
                                 :auto-reload?  false}
                          :cljsbuild {:builds {:app {:compiler {:main "minimal-chat.prod"}}}}}}

  :aliases {"auto-test" ["do" "clean"
                         ["cljsbuild" "auto" "test2"]]})
