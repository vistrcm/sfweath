(ns sfweath.core
  (:require [net.cgrand.enlive-html :as html]
            [sfweath.openai]
            [sfweath.telegram]
            [clojure.string :as str])
  (:import [java.util Base64]))

(def base-url
  "https://forecast.weather.gov/product.php?site=NWS&issuedby=MTR&product=AFD&format=txt&version=1&glossary=0")

(defn exit-if-not-set!
  [name val]
  (when (or (nil? val)
            (= "" val))
    (println (str "please set " name))
    (System/exit 1)))

(comment
  (exit-if-not-set! "namehel" "hello")
  (exit-if-not-set! "anothername" nil)
  (exit-if-not-set! "token?" "")
  #_())

(def openapi-key (System/getenv "OPENAI_API_KEY"))

(def telegram-token (System/getenv "TELEGRAM_BOT_TOKEN"))

(def telegram-channel (System/getenv "TELEGRAM_CH"))

(def send-probability
  (let [val (System/getenv "SEND_PROBABILITY")]
    (if (nil? val)
      1.0
      (new Double val))))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn fetch-page
  []
  (fetch-url base-url))

(defn extract-text
  [page]
  (first (html/texts (html/select page [:pre.glossaryProduct]))))

(defn r-body
  [prev current]
  (sfweath.openai/prep-body (str/replace current "&&" "")
                            (str/replace prev    "&&" "")))

(defn fetch-summary
  [prev current]
  (-> (sfweath.openai/request openapi-key (r-body prev current))
      :body
      :choices
      first
      :message
      :content))

(defn prep-image
  [summary full-text]
  (let [ dalle-prompt (sfweath.openai/create-image-prompt openapi-key summary full-text)
        resp (sfweath.openai/generate-image openapi-key dalle-prompt)
        img-b64 (-> resp :body :data first :b64_json)
        img-bytes (.decode (Base64/getDecoder) img-b64)]
    img-bytes))

(defn -main [& _]
  ;; check if everything is set
  (exit-if-not-set! "OPENAI_API_KEY" openapi-key)
  (exit-if-not-set! "TELEGRAM_BOT_TOKEN" telegram-token)

  (let [prev (slurp "afd")
        page (fetch-page)
        text (extract-text page)
        summary (fetch-summary prev text)
        img (prep-image summary text)]
    (spit "afd" text)
    (spit "afd.sum" summary)
    (if (and (some? telegram-channel)
             (<= (rand) send-probability))
      (sfweath.telegram/send-photo telegram-token telegram-channel img summary)
      (println (str "skipping telegram message. prob: " send-probability ". ch: " telegram-channel)))
    (println summary)))


(comment
  (-main)

  (import '[java.io FileOutputStream])

  (let [summary (slurp "afd.sum")
        full-text (slurp "afd")
        dalle-prompt (sfweath.openai/create-image-prompt openapi-key summary full-text)
        resp (sfweath.openai/generate-image openapi-key dalle-prompt)
             img-b64 (-> resp :body :data first :b64_json)
        img-bytes (.decode (Base64/getDecoder) img-b64)]
    (with-open [out (java.io.FileOutputStream. "img-tst.png")]
      (.write out img-bytes))
    (spit "img-tst.txt" img-b64)
    (sfweath.telegram/send-photo telegram-token put-your-chat-id-here  img-bytes "Weather visualization"))
  #_())
