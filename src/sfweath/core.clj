(ns sfweath.core
  (:require [net.cgrand.enlive-html :as html]))

(def base-url
  "https://forecast.weather.gov/product.php?site=NWS&issuedby=MTR&product=AFD&format=txt&version=1&glossary=0")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def page
  (fetch-url base-url))

(def text
  (first (html/texts (html/select page [:pre.glossaryProduct]))))

(defn -main [& args]
  (println text))
