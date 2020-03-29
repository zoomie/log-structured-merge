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
  (let [tuple (str/split raw #":")
        key (first tuple)
        value (second tuple)]
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
(defn setup-db [data-list]
  (loop [l data-list]
    (if (> 2 (count l))
      nil
      (do 
        (let [key (first l)
              value (second l)]
          (update-db key value))
        (recur (rest (rest l)))))))
