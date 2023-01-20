(ns core)

(defn plus [a b]
  (+ a b))

(defn run [opts]
  (println "Hello world, the sum of 2 and 2 is" (plus 2 2)))