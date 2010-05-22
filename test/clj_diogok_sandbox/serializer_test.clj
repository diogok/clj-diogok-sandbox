(ns clj-diogok-sandbox.serializer-test
  (:use [clj-diogok-sandbox.serializer] :reload-all)
  (:require [clojure.contrib.duck-streams :as io])
  (:use [clojure.test]))

(deftest serialize-string
   (is (= "\"foo\"" (serialize "foo"))))

(deftest serialize-string-with-line-break
   (is (= "\"foo\\nbar\"" (serialize "foo\nbar"))))

(deftest serialize-vector
   (is (= "[1 2 \"foo\"]" (serialize [1 2 "foo"]))))

(deftest serialize-map
   (is (= "#=(clojure.lang.PersistentArrayMap/create {:key \"value\", \"key\" 123, \"name\" \"foo\"})"
          (serialize {:key "value", "key" 123, "name" "foo"}))))

(deftest serialize-all
   (is (= "#=(clojure.lang.PersistentArrayMap/create {:key \"value\", \"key\" [#=(clojure.lang.PersistentArrayMap/create {:foo \"bar\"}) 2 3], \"name\" \"foo\"})"
          (serialize {:key "value", "key" [{:foo "bar"} 2 3] , "name" "foo"}))))

(deftest unserialize-string
   (is (= "foo" (unserialize "\"foo\""))))

(deftest unserialize-string-with-line-break
   (is (= "foo\nbar" (unserialize "\"foo\\nbar\""))))

(deftest unserialize-all
   (is (= 
         {:key "value", "key" [{:foo "bar"} 2 3] , "name" "foo"}
         (unserialize "#=(clojure.lang.PersistentArrayMap/create {:key \"value\", \"key\" [#=(clojure.lang.PersistentArrayMap/create {:foo \"bar\"}) 2 3], \"name\" \"foo\"})")
         )))

(.delete (java.io.File. "test.txt"))
(deftest save-load-file
         (let [obj {:key "value", "key" [{:foo "bar"} 2 3] , "name" "foo"}]
           (save-to-file obj "test.txt")
           (let [loaded (load-from-file "test.txt")]
             (is (= obj loaded))
           )))
(.deleteOnExit (java.io.File. "test.txt"))

