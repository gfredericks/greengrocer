(ns greengrocer.test.core
  (:use [greengrocer.core] :reload)
  (:use greengrocer.test.meta-test
        greengrocer.test.apps)
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

(deftest session-test
  (this-greengrocer-test-should-pass
    (binding [*app* session-app]
      (->
        (GET "/")
        (should-not-see "tacos")
        (submit-form-with
          "item-form"
          {:item "tacos"})
        (follow-redirect)
        (should-see "tacos"))))
  (this-greengrocer-test-should-fail
    (binding [*app* session-app]
      (->
        (GET "/")
        (should-not-see "tacos")
        (submit-form-with
          "item-form"
          {:item "tacos"}))
      (->
        (GET "/")
        (should-see "tacos")))))
