(ns clj-diogok-sandbox.serializer
  (:require [clojure.contrib.duck-streams :as io]))

(defn serialize [obj]
  "Serialize an object into a string"
  (binding [*print-dup* true] (pr-str obj)))

(defn unserialize [string]
  "Unserialize and object from a string"
  (with-in-str string (read)))

(defn mk-file [path]
  "Returns a File, creating it and it's path if needed"
  (let [file (.getAbsoluteFile (io/file-str path))]
    (if-not (.exists (.getParentFile file)) (io/make-parents file))
    (if-not (.exists file) (.createNewFile file))
    file
  )
)

(defn save-to-file [obj path]
  "Serialize object and save to file"
  (io/spit (mk-file path) (serialize obj)))

(defn load-from-file [path]
  "Unserialize file to obj"
  (unserialize (io/slurp* (mk-file path))))
