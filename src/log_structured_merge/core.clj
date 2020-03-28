(ns log-structured-merge.core
  (:gen-class)
  (:require [clojure.string :as str]))

(def memetable {})

; Storing data
(defn dict-length [dict]
  (reduce (fn [count _] (inc count)) 0 dict))

(defn to-file [dict]
  (doseq [tuple dict]
    (let [key (nth tuple 0)
          value (nth tuple 1)]
      (let [in-txt (apply str key ":" value "\n")]
        (spit "data.txt" in-txt :append true)))))

(defn update-db [key value]
  (if (<= 2 (dict-length memetable))
    (do 
      (to-file memetable)
      (def memetable {})))
  (def memetable (assoc memetable key value)))


; Getting data
(defn tuple-saver [dict raw]
  (let [tuple (str/split raw #"-")
        key (nth tuple 0)
        value (nth tuple 1)]
    (assoc dict key value)))

(defn load-from-file []
  (let [text (str/split (slurp "data.txt") #"\n")]
    (reduce tuple-saver {} text)))

(defn get-db [key]
  (if (contains? memetable key)
   (memetable key)
   (let [dict (load-from-file)]
    (dict key))))


; Setup for development
(def setup-data ["a" 1 "b" 2 "c" 3])

(loop [l setup-data]
  (if (> 2 (count l))
    nil
    (do 
      (let [key (nth l 0)
            value (nth l 1)]
        (println key value))
      (recur (rest (rest l))))))

