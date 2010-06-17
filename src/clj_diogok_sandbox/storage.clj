(ns clj-diogok-sandbox.storage
  (:require [clojure.contrib.duck-streams :as io])
  (:use clj-diogok-sandbox.serializer))

(defn tuple [index value]
  "Create a tuple with the index, the value and current time (UNIX time)"
  {:key index :value value :timestamp (long (/ (.getTime (java.util.Date.)) 1000))})

(defn bucket
  "Create/restore a bucket"
  ([label] { :name    label
             :db      (mk-file (str label ".db"))
             :queue   (agent nil)
             :idx     (mk-file (str label ".idx"))
             :indexes (atom (load-from-file (str label ".idx")))
            })
)


(defn put!
  "Store a object on the bucket"
  ([bucket index obj](put! bucket (tuple index obj)))
  ([bucket obj] (let [arr  (io/to-byte-array (serialize obj))]
                      (await (send-off (:queue bucket)
                         (fn [a] (let [end  (:last @(:indexes bucket))
                                       pos  (if-not (nil? end) end 0)
                                       len  (count arr)
                                       idx  {:start pos :length len :key (:key obj)}]
                               (swap! (:indexes bucket) #(assoc % (md5 (:key obj)) idx))
                               (swap! (:indexes bucket) #(assoc % :last (+ pos len)))
                               (io/append-spit (:db bucket) (serialize obj))
                               (io/spit (:idx bucket) (serialize @(:indexes bucket)))
                             )
                        ))
                     )
                     obj
                ))
)

(defn get!
  "Return the objects of a bucket of the object for given index"
  ([bucket] (map #(get! bucket (:key (get @(:indexes bucket) %)))
                 (filter string? (keys @(:indexes bucket)))))
  ([bucket index] (let [index (get @(:indexes bucket) (md5 index))]
                    (unserialize  (partial-read (:db bucket) (:start index) (:length index))))))
