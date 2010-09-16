(ns clj-diogok-sandbox.couchdb
 (:use clojure.contrib.json)
 (:use clojure.contrib.http.connection) 
 (:refer-clojure :exclude [get key]) 
 (:require [clojure.contrib.io :as io]))

    (defn server
     "Wraps a server"
     [host port] {:server (str "http://" host ":" port)}) 

    (defn db
     "Wraps a database on a server"
     [server db] (assoc server :db db)) 

    (defn- oops  [ex conn] 
     "Handle problems1"
     (read-json (io/slurp* (.getErrorStream conn))))

    (defn- connect [url] 
     "Starts a connection on server"
     (doto (http-connection url) 
       (.setRequestProperty "User-Agent" "Diogok-Clojure"))) 

    (defn- put-1 [url object]
     "Perform a PUT/POST on the url, 
       sending a json and reading the response. 
      Return false on failure"
     (let [conn (doto (connect url) (.setDoOutput true) 
                 (.setRequestProperty "Content-Type" "application/json"))]
      (try (with-open [out (.getOutputStream conn)]
        (do
         (spit out  (json-str object))
         (read-json (io/slurp* (.getInputStream conn)))))
       (catch Exception e (oops e conn))))) 

    (defn put 
     "Put a value on the database, return false on failure"
     ([{server :server db :db} value] (put-1 (str server "/" db) value )) 
     ([db key value] (put db (assoc value :_id key)))) 

    (defn- get-1 [url]
     "Read a json from url, return false on failure"
     (let [conn  (connect url)]
      (try 
         (read-json (io/slurp* (.getInputStream conn)))
       (catch Exception e (oops e conn))))) 

    (defn get
     "Get a value from the database, return false on failure"
     [{server :server db :db} key] (get-1 (str server "/" db "/" key)))

    (defn view
     "Wraps view information"
     ([db design view-name] (assoc db :design design :view view-name)))

     (defn query 
      "Query a view"
     ([{server :server db :db design :design view-name :view-name}]
      (get-1 (str server "/" db "/_design/" design "/_view/" view-name)))
     ([{server :server db :db design :design view-name :view-name} key]
      (get-1 (str server "/" db "/_design/" design "/_view/" view-name
                  "?key=\"" key "\""))))

     (defn delete
      "Delete a value from database"
      ([{server :server db :db} obj]
       (let [conn (doto (connect (str server "/" db "/" (obj :_id)))
                   (.setRequestMethod "DELETE")
                   (.setRequestProperty "If-Match" (obj :_rev)))]
        (try 
         (read-json (io/slurp* (.getInputStream conn)))
         (catch Exception e (oops e conn))))))
