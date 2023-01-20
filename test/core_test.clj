(ns core-test
  (:require [clojure.test :refer [is deftest]]
            [core :refer [plus]]))

(deftest adding-numbers
  (is (= 4 (plus 2 2))))