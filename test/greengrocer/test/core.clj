(ns greengrocer.test.core
  (:use [greengrocer.core] :reload)
  (:use greengrocer.test.meta-test)
  (:use [clojure.test]))

(deftest http-success-test
  (this-greengrocer-test-should-pass
    (should-be-successful
      {:status 200}))
  (this-greengrocer-test-should-fail
    (should-be-successful
      {:status 404}))
  (this-greengrocer-test-should-pass
    (should-not-be-successful
      {:status 404}))
  (this-greengrocer-test-should-fail
    (should-not-be-successful
      {:status 300})))
