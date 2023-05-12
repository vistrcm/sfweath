(ns sfweath.core
  (:require [net.cgrand.enlive-html :as html]
            [sfweath.openai]
            [sfweath.telegram]
            [clojure.string :as str]))

(def base-url
  "https://forecast.weather.gov/product.php?site=NWS&issuedby=MTR&product=AFD&format=txt&version=1&glossary=0")

(defn get-param-or-exit [name]
  (let [val (System/getenv name)]
    (if (nil? val)
      (do
        (println (str "please set " name))
        (System/exit 1))
      val)))

(def openapi-key
  (get-param-or-exit "OPENAI_API_KEY"))

(def telegram-token
  (get-param-or-exit "TELEGRAM_BOT_TOKEN"))

(def telegram-channel
  (get-param-or-exit "TELEGRAM_CH"))

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

(defn -main [& _]
  (spit "afd" text)
  (spit "afd.sum" summary)
  (sfweath.telegram/send-message telegram-token telegram-channel summary))
