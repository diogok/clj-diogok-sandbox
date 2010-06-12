(ns clj-diogok-sandbox.serializer-test
  (:use [clj-diogok-sandbox.serializer] :reload-all)
  (:require [clojure.contrib.duck-streams :as io])
  (:use [clojure.test]))

; foo -> dba520e335c06ba9240a978e9455878
(deftest md5-foo
         (is (= "dba520e335c06ba9240a978e9455878") (md5 "foo")))

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

(deftest make-file-test
    (let [file (mk-file "test-me.txt")]
      (is (.exists file))
      (.deleteOnExit file)))

(deftest make-file-of-file-test
    (let [f1 (java.io.File. "test-me2.txt")
          file (mk-file f1)]
      (is (.exists file))
      (.deleteOnExit file)))

(deftest make-file-test-parents
    (let [file (mk-file "hello/test-me.txt")]
      (is (.exists file))
      (.deleteOnExit file)
      (.deleteOnExit (java.io.File. "hello/"))))

(deftest save-load-file
         (let [obj {:key "value", "key" [{:foo "bar"} 2 3] , "name" "foo"}]
           (.delete (java.io.File. "test.txt"))
           (save-to-file obj "test.txt")
           (let [loaded (load-from-file "test.txt")]
             (is (= obj loaded)))
           (.deleteOnExit (java.io.File. "test.txt"))
         ))
