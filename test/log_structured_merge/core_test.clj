(ns log-structured-merge.core-test
  (:require [clojure.test :refer :all]
            [log-structured-merge.core :refer :all]))

(def data ["a" 1 "b" 2 "c" 3])
(setup-db data)

(deftest a-test
  (is (= 1 (get-db "a"))))

(deftest a-test
  (is (= 3 (get-db "c"))))

