(defproject sfweath "0.1.0-SNAPSHOT"
  :description "play with afd"
  :url "https://github.com/vistrcm/sfweath"
  :license {:name "MIT"
            :url "https://github.com/vistrcm/sfweath/blob/main/LICENSE.md"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [enlive "1.1.6"]
                 [clj-http "3.12.3"]
                 [cheshire "5.11.0"]
                 [org.clojure/tools.reader "1.3.6"]]
  :profiles {:repl {:plugins [[cider/cider-nrepl "0.30.0"]]}}
  :repl-options {:init-ns sfweath.core}
  :main sfweath.core)
