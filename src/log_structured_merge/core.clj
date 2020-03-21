(ns log-structured-merge.core
  (:gen-class))


(def memtable {})

(defn find-length [dict]
 (reduce (fn [counter _] (inc counter)) 0 dict))


(defn write-to-data [dict]
  (for [tuple dict]
    (do 
      (println tuple)
      (let [in-string (apply str (conj tuple "\n"))]
        (spit "data.txt" in-string :append true)))))


(defn update-dict [key value]
  (do
    (if (> (find-length memtable) 1)
        (write-to-data memtable))
    (def memtable (assoc memtable key value))))

(update-dict "key1" "value")
(update-dict "key2" "value")
(update-dict "key3" "value")


(defn -main
  "Entry point"
  [& args]
  (run-lsm "Hello, World!"))


