(ns clj-diogok-sandbox.storage-test
  (:use [clj-diogok-sandbox.storage] :reload-all)
  (:use clj-diogok-sandbox.serializer)
  (:use [clojure.test]))

(deftest mk-tuple
         (let [foo (tuple "foo" "bar")]
           (is (= "foo" (:key foo)))
           (is (= "bar" (:value foo)))
         ))

(deftest do-put-get
         (let [buck (bucket "foo/bar")
                     t1  (put! buck (tuple "diogok" "diogok@me.com"))
                     t2  (put! buck (tuple "diogoh" "diogoh@me.com"))
                     t22 (put! buck (tuple "diogoh" "diogho@me.com"))
                     t3  (put! buck (tuple "diogog" "diogog@me.com"))]
           (is (= t1 (get! buck "diogok")))
           (is (= t22 (get! buck "diogoh")))
           (is (= (list t3 t22 t1) (get! buck)))
         ))

(deftest do-put-get-consistency
         (let [buck  (bucket    "foo/bar2")
               t1    (put! buck (tuple "diogok" "diogok@me.com"))]
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
            

(.deleteOnExit (java.io.File. "foo/bar.idx"))
(.deleteOnExit (java.io.File. "foo/bar.db"))
(.deleteOnExit (java.io.File. "foo/bar2.idx"))
(.deleteOnExit (java.io.File. "foo/bar2.db"))
(.deleteOnExit (java.io.File. "foo/bar3.idx"))
(.deleteOnExit (java.io.File. "foo/bar3.db"))
