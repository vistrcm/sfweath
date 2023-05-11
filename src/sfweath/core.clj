(ns sfweath.core
  (:require [net.cgrand.enlive-html :as html]
            [sfweath.openai]))

(def base-url
  "https://forecast.weather.gov/product.php?site=NWS&issuedby=MTR&product=AFD&format=txt&version=1&glossary=0")

(def openapi-key
  (System/getenv "OPENAI_API_KEY"))

(if (nil? openapi-key)
  (System/exit 1))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def page
  (fetch-url base-url))

(def text
  (first (html/texts (html/select page [:pre.glossaryProduct]))))

(def r-body (sfweath.openai/prep-body text))

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
