(ns sfweath.openai
  (:require [clj-http.client :as client]
            [cheshire.core :as ch]))

(def url "https://api.openai.com/v1/chat/completions")

(def initial-setup (slurp "setup.txt"))
(def prompt (slurp "prompt.txt"))

(defn prep-body [afd]
  (ch/generate-string
   {:model "gpt-4"
    :messages [{:role "system" :content initial-setup}
               {:role "user" :content (str prompt "\n" afd)}]
    :max_tokens 200
    :temperature 1.2
    :n 1}
   {:escape-non-ascii true}))

(defn request [key body]
  (client/post url
               {:body body
                :content-type :json
                :headers {"Authorization" (str "Bearer " key)}
                :as :json}))
