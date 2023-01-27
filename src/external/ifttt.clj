(ns external.ifttt
  (:require [hato.client :as hc]))

(def base-url "https://maker.ifttt.com/trigger/")

(def api-key (System/getenv "IFTTT_API_KEY"))

(defn trigger-ifttt
  [event-name payload]
  (let [url (str base-url event-name "/json/with/key/" api-key)]
    (hc/post url {:form-params payload
                  :content-type :json})))

(comment
  (trigger-ifttt "email" {:random "asdf"}))
