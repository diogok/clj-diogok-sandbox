(ns clj-diogok-sandbox.http-test
  (:use [clj-diogok-sandbox.http] :reload-all)
  (:use [clojure.test]))

(deftest treat-headers-test
         (let [headers (new java.util.HashMap)
               values (new java.util.Vector)]
           (.add values "bar")
           (.put headers "foo" values)
           (is (= "bar" (get (treat-headers headers ) "foo" )))
         ))

(def req (http-get "http://manifesto.blog.br"))

(deftest http-get-basic-content
    (is (.contains (:content req) "http://github.com/diogok" )))

(deftest http-get-basic-reponse-code
    (is (= 200 (:response-code req))))

(deftest http-get-basic-headers
    (is (= "Apache" (get (:headers req) "Server" ))))


