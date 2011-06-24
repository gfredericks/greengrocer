(ns greengrocer.test.core
  (:use [greengrocer.core] :reload)
  (:use greengrocer.test.meta-test)
  (:use [clojure.test]))

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
