(ns sfweath.core
  (:require [net.cgrand.enlive-html :as html]
            [sfweath.openai]
            [clojure.string :as str]))

(def base-url
  "https://forecast.weather.gov/product.php?site=NWS&issuedby=MTR&product=AFD&format=txt&version=1&glossary=0")

(def openapi-key
  (System/getenv "OPENAI_API_KEY"))

(if (nil? openapi-key)
  (do
    (println "please set OPENAI_API_KEY")
    (System/exit 1)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def page
  (fetch-url base-url))

(def text
  (first (html/texts (html/select page [:pre.glossaryProduct]))))

(def r-body (sfweath.openai/prep-body (str/replace text "&&" "")))

(def summary
  (-> (sfweath.openai/request openapi-key r-body)
      :body
      :choices
      first
      :message
      :content))

(defn -main [& args]
  (spit "afd" text)
  (spit "afd.sum" summary))
