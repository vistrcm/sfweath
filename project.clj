(defproject sfweath "0.1.0-SNAPSHOT"
  :description "play with afd"
  :url "https://github.com/vistrcm/sfweath"
  :license {:name "MIT"
            :url "https://github.com/vistrcm/sfweath/blob/main/LICENSE.md"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [enlive "1.1.6"]]
  :repl-options {:init-ns sfweath.core}
  :main sfweath.core)
