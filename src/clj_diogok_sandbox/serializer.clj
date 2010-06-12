(ns clj-diogok-sandbox.serializer
  (:require [clojure.contrib.duck-streams :as io]))

(defn serialize [obj]
  "Serialize an object into a string"
  (binding [*print-dup* true] (pr-str obj)))

(defn unserialize [string]
  "Unserialize and object from a string"
  (if (= "" string) nil
    (with-in-str string (read))))

(defn md5 [obj]
  "Calculate md5sum of object"
  (let [bytes (.getBytes (serialize obj))] 
    (.toString (new java.math.BigInteger 1
      (.digest (java.security.MessageDigest/getInstance "MD5") bytes)) 16)))

(defn mk-file [path]
  "Returns a File, creating it and it's path if needed"
  (let [file (.getAbsoluteFile (if (string? path) (io/file-str path) path )) ]
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

(defn partial-read [file start length]
  "Read part of the file, tricky."
  (let [stream (java.io.FileInputStream. file)
        buffer (make-array Byte/TYPE length)]
     (try (do (.skip stream start) (.read stream buffer))
       (catch Exception e (.printStackTrace e))
       (finally (.close stream)))
    (io/slurp* (java.io.ByteArrayInputStream. buffer))
   ))

