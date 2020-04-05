(ns log-structured-merge.core
  (:gen-class)
  (:require [clojure.string :as str]))

(def memetable {})

; Storing data
(defn dict-length [dict]
  (reduce (fn [count _] (inc count)) 0 dict))

(def ^:dynamic *datafile-num* 0)

(defn get-data-file []
  (def ^:dynamic *datafile-num* (inc *datafile-num*))
  (let [file (apply str *datafile-num* "data.txt")]
    (apply str "datadir/" file)))


(defn to-file [dict]
  (let [data-file (get-data-file)]
    (doseq [tuple dict]
      (let [key (first tuple)
            value (second tuple)]
        (let [in-txt (apply str key ":" value "\n")]
          (spit data-file in-txt :append true))))))

(defn update-db [key value]
  (if (<= 2 (dict-length memetable))
    (do 
      (to-file memetable)
      (def memetable {})))
  (def memetable (assoc memetable key value)))

(update-db "a" 1)
(update-db "b" 2)
(update-db "c" 3)
(update-db "d" 4)
(println memetable)

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
