(ns external.iftt
  (:require [hato.client :as hc]))

(def base-url "https://maker.ifttt.com/trigger/")

(def api-key (System/getenv "IFTT_API_KEY"))

(defn trigger-iftt
  [event-name payload]
  (let [url (str base-url event-name "/json/with/key/" api-key)]
    (hc/post url {:form-params payload
                  :content-type :json})))

(comment
  (trigger-iftt "ted_transcript" {:random "asdf"}))
