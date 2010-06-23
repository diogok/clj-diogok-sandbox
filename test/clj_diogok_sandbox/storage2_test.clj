(ns clj-diogok-sandbox.storage2-test
  (:use [clj-diogok-sandbox.storage2] :reload-all)
  (:use clj-diogok-sandbox.serializer)
  (:require [clojure.contrib.duck-streams :as io])
  (:use [clojure.test]))

(deftest mk-tuple
         (let [foo (tuple "foo" "bar")]
           (is (= "foo" (:key foo)))
           (is (= "bar" (:value foo)))
         ))

(deftest replay-file-empty
         (let [buck (bucket "test2")]
           (is (= {} @(buck :indexes)))
         ))

(deftest replay-file
         (let [file (mk-file "test.db")]
           (io/append-spit file (str "12345678901234561234567890123456 = " (serialize {:foo "bar"}) "\n"))
           (io/append-spit file (str "12345678901234561234567890123455 = " (serialize {:foo "bar"}) "\n"))
           (io/append-spit file (str "12345678901234561234567890123454 = " (serialize {:foo "bar"}) "\n"))
           (io/append-spit file (str "12345678901234561234567890123453 = " (serialize {:foo "bar"}) "\n"))
           (io/append-spit file (str "12345678901234561234567890123454 = " (serialize {:foo "bar"}) "\n"))
           (let [buck     (bucket "test"),
                 indexes @(:indexes buck)
                 lines   @(:last buck)]
             (is (= 0 (get indexes "12345678901234561234567890123456")))
             (is (= 1 (get indexes "12345678901234561234567890123455")))
             (is (= 4 (get indexes "12345678901234561234567890123454")))
             (is (= 3 (get indexes "12345678901234561234567890123453")))
             (is (= 5 lines))
           )
         ))

(deftest do-put-get
         (let [buck (bucket "foo/bar")
                     t1  (tuple "diogok" "diogok@me.com")
                     t2  (tuple "diogoh" "diogoh@me.com")
                     t22 (tuple "diogoh" "diogho@me.com")
                     t3  (tuple "diogog" "diogog@me.com")]
           (put! buck t1)
           (put! buck t2)
           (put! buck t22)
           (put! buck t3)
           (is (= t1 (get! buck "diogok")))
           (is (= t22 (get! buck "diogoh")))
           (is (= (list t3 t22 t1) (get! buck)))
         ))

(deftest do-put-get-consistency
         (let [buck  (bucket    "foo/bar2")
               t1    (tuple "diogok" "diogok@me.com")]
               (put! buck t1)
               (let [buck2 (bucket "foo/bar2")]
                 (is (= t1 (get! buck2 "diogok"))))
         ))

(deftest do-put-get-concurrency-consistency
         (let [ buck (bucket "foo/bar3")
                t1 (future (dotimes [n 100] (let [obj (tuple (str "a" n) (str "a" n))]
                                      (put! buck obj))))
                t2 (future (dotimes [n 100] (let [obj (tuple (str "b" n) (str "b" n))]
                                      (put! buck obj)))) ]
           [@t1 @t2]
           (is (= "a99" (:value (get! buck "a99"))))
           (is (= "b98" (:value (get! buck "b98"))))
           (let [buck2  (bucket "foo/bar3")]
             (is (= "a88" (:value (get! buck2 "a88"))))
             (is (= "b89" (:value (get! buck2 "b89")))))
         ))
            

(.deleteOnExit (java.io.File. "test.db"))
(.deleteOnExit (java.io.File. "test2.db"))
(.deleteOnExit (java.io.File. "foo/bar.db"))
(.deleteOnExit (java.io.File. "foo/bar2.db"))
(.deleteOnExit (java.io.File. "foo/bar3.db"))
