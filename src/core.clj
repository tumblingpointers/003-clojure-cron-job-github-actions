(ns core
  (:require [jobs.ted-talk-transcript :as ted-talk-transcript]))

(defn plus [a b]
  (+ a b))

(defn run [{:keys [job input]}]
  (println "Running job" job)
  (case (str job)
    "ted-talk-transcript" (ted-talk-transcript/run input)
    "default"))