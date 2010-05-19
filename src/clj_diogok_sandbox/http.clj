(ns clj-diogok-sandbox.http
  (:use clojure.contrib.http.connection )
  (:require [clojure.contrib.duck-streams :as io]))

(defn treat-headers [headers]
  "Parse headers Map<String,List<String>>"
  (reduce merge (map
      (fn [header] {(.getKey header) (.get (.getValue header) 0)}) 
      (.entrySet headers))))

(defn http-get [url]
  "Get the URL content and headers"
  (let [conn (http-connection url)
        content (future (io/slurp* (.getInputStream conn))) ]
    {:headers (treat-headers (.getHeaderFields conn) )
     :response-code (.getResponseCode conn)
     :response-message (.getResponseMessage conn)
     :content @content}
  ))

