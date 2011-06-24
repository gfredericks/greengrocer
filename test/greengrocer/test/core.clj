(ns greengrocer.test.core
  (:use [greengrocer.core] :reload)
  (:use [clojure.test]))

(defmacro get-test-results
  [& body]
  `(binding
     [*report-counters* (ref *initial-report-counters*),
      *testing-vars* (list),
      *testing-contexts* (list),
      *test-out* (new java.io.FileWriter "/dev/null")]
     ~@body
     @*report-counters*))

(defmacro should-fail
  [& body]
  `(let [res# (get-test-results ~@body)]
     (is (pos? (+ (:fail res#) (:error res#))))))

(defmacro should-succeed
  [& body]
  `(let [res# (get-test-results ~@body)]
     (is (zero? (+ (:fail res#) (:error res#))))
     (is (pos? (:pass res#)))))

(deftest should-fail-test
  (should-fail (is (zero? 4))))

(deftest should-succeed-test
  (should-succeed (is (pos? 4))))

(deftest http-success-test
  (should-succeed
    (should-be-successful
      {:status 200}))
  (should-fail
    (should-be-successful
      {:status 404}))
  (should-succeed
    (should-not-be-successful
      {:status 404}))
  (should-fail
    (should-not-be-successful
      {:status 300})))
