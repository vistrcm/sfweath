(ns sfweath.telegram
  (:require [clj-http.client :as client]))

(def base-url "https://api.telegram.org/")

(defn method-url [auth method]
  (str base-url "bot" auth "/" method))

(defn request
  ([auth method params]
   (client/post (method-url auth method)
                {:form-params params
                 :as :json}))
  ([auth method]
   (request auth method nil)))

(defn- get-udates [auth]
  "used mostly to get chat id for testing"
  (request auth "getUpdates"))

(defn send-message [auth chat-id msg]
  (request auth "sendMessage" {:chat_id chat-id
                               :text msg}))

(defn send-photo
  ([auth chat-id photo-bytes]
   (send-photo auth chat-id photo-bytes nil))
  ([auth chat-id photo-bytes caption]
   (let [parts [{:name "chat_id" :content (str chat-id)}
                {:name "photo"
                 :content photo-bytes
                 :filename "weather.png"}]]
     (client/post (method-url auth "sendPhoto")
                  {:multipart (if caption
                                (conj parts {:name "caption" :content caption})
                                parts)
                   :as :json}))))
