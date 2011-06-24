(ns greengrocer.test.meta-test
  (:use clojure.test))

(defmacro get-test-results
  [& body]
  `(binding
     [*report-counters* (ref *initial-report-counters*),
      *testing-vars* (list),
      *testing-contexts* (list),
      *test-out* (new java.io.FileWriter "/dev/null")]
     ~@body
     @*report-counters*))

(defmacro this-greengrocer-test-should-fail
  [& body]
  `(let [res# (get-test-results ~@body)]
     (is (pos? (+ (:fail res#) (:error res#))))))

(defmacro this-greengrocer-test-should-pass
  [& body]
  `(let [res# (get-test-results ~@body)]
     (is (zero? (+ (:fail res#) (:error res#))))
     (is (pos? (:pass res#)))))

(deftest this-greengrocer-test-should-fail-test
  (this-greengrocer-test-should-fail (is (zero? 4))))

(deftest this-greengrocer-test-should-pass-test
  (this-greengrocer-test-should-pass (is (pos? 4))))
