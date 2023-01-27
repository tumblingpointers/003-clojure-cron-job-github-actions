(ns jobs.ted-talk-transcript
  (:require [hato.client :as hc]
            [cheshire.core :as cheshire]
            [clojure.string :as string]
            [external.iftt :refer [trigger-iftt]]))

(def base-url "https://www.ted.com")

(defn- search-talks [query]
  (let [query-url   (str base-url "/talks?sort=relevance&q=" query)
        results     (-> (hc/get query-url)
                        :body)
        links       (re-seq #"/talks/(.*)'" results)
        video-ids   (distinct (map second links))]
    video-ids))

(defn- get-transcript
  [video-id]
  (let [gql (str "{translation(language:\"en\", videoId:\""
                 video-id
                 "\") {id paragraphs { cues { text }}}}")
        response (hc/post (str base-url "/graphql") {:form-params {:operationName nil
                                                                   :query gql}
                                                     :content-type :json})
        json (-> response
                 :body
                 (cheshire/parse-string true))
        paragraphs (-> json
                       :data
                       :translation
                       :paragraphs)
        paragraphs (map :cues paragraphs)
        paragraphs (map #(map :text %) paragraphs)]
    (map #(string/join " " %) paragraphs)))

(defn- remove-suffix-if-exists
  [str]
  (string/replace str #"(.*) \| TED" "$1"))

(defn run
  [query]
  (let [query          (remove-suffix-if-exists query)
        talk-video-ids (search-talks query)]
    (some->> talk-video-ids
             first
             get-transcript
             (trigger-iftt "ted_transcript")
             :body)))

(comment
  (run "A Socialist Perspective on the Pursuit of Happiness | Aaron Bastani | TED"))