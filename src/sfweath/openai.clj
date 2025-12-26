(ns sfweath.openai
  (:require [clj-http.client :as client]
            [cheshire.core :as ch]))

(def url "https://api.openai.com/v1/chat/completions")

(def initial-setup (slurp "setup.txt"))
(def prompt (slurp "prompt.txt"))

(defn prep-body [afd]
  (ch/generate-string
   {:model "gpt-5-mini"
    :messages [{:role "system" :content initial-setup}
               {:role "user" :content (str prompt "\n" afd)}]}
   {:escape-non-ascii true}))

(defn request [key body]
  (try
    (client/post url
                 {:body body
                  :content-type :json
                  :headers {"Authorization" (str "Bearer " key)}
                  :as :json})
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        (println "OpenAI API error:" (:status data))
        (println (:body data)))
      (System/exit 1))
    (catch Exception e
      (println "Request failed:" (.getMessage e))
      (System/exit 1))))
