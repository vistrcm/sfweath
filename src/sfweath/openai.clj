(ns sfweath.openai
  (:require [clj-http.client :as client]
            [cheshire.core :as ch]))

(def url "https://api.openai.com/v1/chat/completions")
(def image-url "https://api.openai.com/v1/images/generations")
(def model "gpt-5-mini")

(def initial-setup (slurp "setup.txt"))
(def prompt (slurp "prompt.txt"))

(defn prep-body [afd]
  (ch/generate-string
   {:model model
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
        (println (:body data))
        (throw (ex-info "OpenAI API request failed"
                        {:status (:status data)
                         :body (:body data)}
                        e))))))

(defn generate-image [key prompt]
  (try
    (client/post image-url
                 {:body (ch/generate-string {:model "gpt-image-1.5"
                                              :prompt prompt
                                              :size "1024x1024"
                                              :n 1})
                  :content-type :json
                  :headers {"Authorization" (str "Bearer " key)}
                  :as :json})
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        (println "OpenAI Image API error:" (:status data))
        (println (:body data))
        (throw (ex-info "OpenAI Image API request failed"
                        {:status (:status data)
                         :body (:body data)}))))))


(comment
  (def resp (generate-image (System/getenv "OPENAI_API_KEY") "a beautiful sunset over mountains"))
  (:body resp)
  #_())
