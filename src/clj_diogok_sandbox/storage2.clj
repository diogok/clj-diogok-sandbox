(ns clj-diogok-sandbox.storage2
  (:require [clojure.contrib.duck-streams :as io])
  (:use clj-diogok-sandbox.serializer))

(defn tuple [index value]
  "Create a tuple with the index, the value and current time (UNIX time)"
  {:key index :value value :timestamp (long (/ (.getTime (java.util.Date.)) 1000))})

(defn put!
  "Store a object on the bucket"
  ([bucket index obj](put! bucket (tuple index obj)))
  ([bucket obj] (await (send-off (bucket :last) 
                    (fn [line]
                      (let [index  (md5 (obj :key))]
                        (io/append-spit (bucket :file) (str index " = " (serialize obj) "\n"))
                        (swap! (bucket :indexes) #(assoc %1 index line))
                        (inc line)))
                  )))
)

(defn unserialize-line
  [lines line] (let [line (nth lines line)]
                 (unserialize (subs line (inc (.indexOf line "="))))))

(defn get!
  "Return the objects of a bucket or the object for given index"
  ([bucket] (with-open [reader (io/reader (bucket :file))] 
              (let [lines (line-seq reader)]
                (doall (map #(unserialize-line lines (val %1)) @(bucket :indexes))))))
  ([bucket index] (with-open [reader (io/reader (bucket :file))]
                    (let [lines(line-seq reader)]
                        (unserialize-line lines (get @(bucket :indexes) (md5 index))
                    ))))
)

(defn replay [bucket]
  "Replay a bucket"
  (with-open [reader  (io/reader (bucket :file))]
    (let [counter (atom -1)
          idx (reduce #(assoc %1 (subs %2 0 (dec (.indexOf %2 "="))) (swap! counter inc)) {} (line-seq reader))]
      (merge bucket {:indexes (atom idx) :last (agent (inc @counter))})
    )
  ))

(defn bucket
  "Create/restore a bucket"
  ([label] (replay {:file (mk-file (str label ".db")) })))
