(ns clj-diogok-sandbox.serializer
  (:require [clojure.contrib.duck-streams :as io]))

(defn serialize [obj]
  (binding [*print-dup* true] (pr-str obj)))

(defn unserialize [string]
  (with-in-str string (read)))

(defn save-to-file [obj path]
  "Serialize object and save to file"
  (io/spit (java.io.File. path) (serialize obj)))

(defn load-from-file [path]
  "Unserialize file to obj"
  (unserialize (io/slurp* (java.io.File. path))))
