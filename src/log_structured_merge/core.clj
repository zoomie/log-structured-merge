(ns log-structured-merge.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:require [org.httpkit.server :as s])
  (:require [clojure.java.shell :as shell]))

(def memetable (ref {}))
(def datafile-num (ref 0))

; Storing data
(defn get-data-file []
  (dosync (alter datafile-num inc))
  (let [file (apply str @datafile-num "data.txt")]
    (apply str "datadir/" file)))

(defn to-file [data-file dict]
  (let [sorted-dict (into (sorted-map) dict)]
    (doseq [tuple sorted-dict]
      (let [[key value] tuple]
        (let [in-txt (apply str key ":" value "\n")]
          (spit data-file in-txt :append true))))))

(defn update-db [key value]
  (if (<= 2 (count @memetable))
    (do 
      (to-file (get-data-file) @memetable)
      (dosync (alter memetable (fn [_] {})))))
  (dosync (alter memetable assoc key value)))

(update-db "b" 1)
(update-db "a" 2)
(update-db "c" 3)
(update-db "d" 4)

; Getting data
(defn tuple-saver [dict raw]
  (let [[key value] (str/split raw #":")]
    (assoc dict key value)))

(defn load-from-file [prefix]
  (let [file (apply str prefix "data.txt")
        path (apply str "datadir/" file)
        text (str/split (slurp path) #"\n")]
    (reduce tuple-saver {} text)))

(defn get-db [key]
  (if (contains? @memetable key)
    (@memetable key)
    (loop [level @datafile-num]
      (if (> 1 level)
        nil
        (let [dict (load-from-file level)]
          (if (contains? dict key)
            (dict key)
            (recur (dec level))))))))

(get-db "c")
(get-db "a")

(defn update-req [req]
  (let [body (:body req)
        bytes (byte-array (:content-length req))
        _ (. body (read bytes))
        text (String. bytes) 
        [key value] (str/split text #"&")]
    (update-db key value)))

(defn get-req [req]
  (let [key (req :query-string)]
    (get-db key)))

(defn app [req]
  (if (= (req :request-method) :post)
    (update-req req)
    (println (get-req req)))
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "New text"})

(defonce server (atom nil))

(reset! server (s/run-server #'app {:port 8001}))

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

; Compation 
(defn load-all-data []
  (loop [level @datafile-num
        data-dict {}]
    (if (> 1 level)
      data-dict
      (let [incoming-dict (load-from-file level)]
        (recur (dec level) (merge data-dict incoming-dict))))))

(defn delete-files-in-datadir []
  (let [files ((shell/sh "ls" "datadir") :out)
        l-files (str/split files #"\n")]
    (doseq [file l-files]
      (let [path (apply str "datadir/" file)]
        (shell/sh "rm" path)))))

(let [data (load-all-data)
      _ (delete-files-in-datadir)]
  (to-file "datadir/1data.txt" data))



; (shell/sh "rm" "datadir/*" :dir "/Users/andrew/work/log-structured-merge")
