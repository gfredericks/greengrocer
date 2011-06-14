(ns greengrocer.core
  "Helper methods for testing Ring web applications."
  (:use [net.cgrand.enlive-html :only [html-resource]])
  (:require [clojure.string :as string])
  (:require [clojure.contrib.string :as ccstr])
  (:require [clojure.test :as test]))

(defn encode-map
  [m]
  (clojure.string/join "&"
    (for [[k v] m :when (not (clojure.string/blank? (str v)))]
      (format "%s=%s" (name k)
              (URLEncoder/encode (if (keyword? v) (name v) (str v)) "UTF-8")))))

(defn decode-query-string
  [s]
  (into {}
        (for [ss (clojure.string/split s #"&")]
          (if-let [[_ k v] (re-matches #"(.*)=(.*)" ss)]
            [k (URLDecoder/decode v)]
            [ss true]))))

;;;
;;; Private vars
;;;

(defn- fail-if-nil
  [arg msg]
  (if arg arg (throw (Exception. msg))))

(defn- parse-body [resp]
  (-> resp :body java.io.StringReader. html-resource first))

(defn find-html-element
  "Returns the first html element encountered for which f returns true."
  [html f]
  (if (f html)
    html
    (when-let [content (:content html)]
      (some #(find-html-element % f) content))))

(defn- content-has-text?
  [content link-text]
  (let [[s] content]
    (and (string? s) (ccstr/substring? link-text s))))

(defn- find-path-for-link
  [resp link-text]
  (->
    resp
    parse-body
    (find-html-element
      (fn [{:keys [tag content]}]
        (and (= :a tag)
             (content-has-text? content link-text))))
    :attrs
    :href))

(defn- find-form-params
  [resp form-id]
  (let [{:keys [method action]}
          (->
            resp
            parse-body
            (find-html-element
              (fn [{:keys [tag attrs]}]
                (and (= :form tag)
                     (= form-id (:id attrs)))))
            :attrs)]
    [(-> method clojure.string/lower-case keyword)
     action]))

(defn- stringize-keys
  [m]
  (let [ks (keys m)]
    (zipmap (map (comp str name) ks)
            (map m ks))))

(defn- check-uri-for-query-string
  [s]
  (if-let [[_ uri qs] (re-matches #"(.*)\?(.*)" s)]
    [uri (decode-query-string qs)]
    [s nil]))
;;;
;;; Public API
;;;

(def *app* nil)

(defn request
  [method uri & opts]
  (let [[uri query-string-params] (check-uri-for-query-string uri),
        {:keys [headers form-params query-params]} (apply hash-map opts),
        h {:request-method method,
           :uri uri},
        h (if headers (assoc h :headers headers) h),
        h (if form-params
            (assoc h :content-type "application/x-www-form-urlencoded",
                    :body (-> form-params encode-map java.io.StringReader.))
            h),
        query-params (merge (or query-params {}) (or query-string-params {})),
        h (if-not (empty? query-params) (assoc h :params (stringize-keys query-params)) h)]
    (*app* h)))

(def GET (partial request :get))
(def POST (partial request :post))

(defn success? [resp] (< (:status resp) 400))
(defn redirect? [resp] (-> resp :status (= 302)))

(defn linebreakify
  [s]
  (string/replace s "&#8209;" "-"))

(defn page-contains?
  [resp text]
  (ccstr/substring? text (linebreakify (:body resp))))

(defn- get-cookie
  [{headers :headers}]
  (if headers
    (if-let [cookie (first (headers "Set-Cookie"))]
      (if-let [m (re-matches #"^([^;]+)(;.*|$)" cookie)]
        (m 1)))))

(defn follow-redirect
  [resp]
  (-> resp
      :headers
      (get "Location")
      (fail-if-nil (str "Response is not a redirect: " (pr-str resp)))
      (GET :headers {"cookie" (get-cookie resp)})))

(defn follow-link
  [resp link-text]
  (-> resp
      (find-path-for-link link-text)
      (fail-if-nil (str "Cannot find link with text: " link-text))
      GET))

(defn submit-form-with
  [resp form-id args]
  (let [[method path] (find-form-params resp form-id)]
    (request method path :form-params args)))

;; Assertion helpers
;;   Each one takes a response object and maybe some args, asserts something
;;   with the clojure.test/is macro, and returns the response object (for
;;   clean threading with ->)

(defn should-be-successful
  [resp]
  (test/is (success? resp))
  resp)

(defn should-not-be-successful
  [resp]
  (test/is (not (success? resp)))
  resp)

(defn should-see
  [resp text]
  (test/is (page-contains? resp text))
  resp)

(defn should-not-see
  [resp text]
  (test/is (not (page-contains? resp text)))
  resp)

(defn should-have
  [resp f]
  (test/is (find-html-element (parse-body resp) f))
  resp)

(defn should-not-have
  [resp f]
  (test/is (not (find-html-element (parse-body resp) f)))
  resp)

(defn should
  "Accepts a function which is called with the response. Passes the
  return value from the function to (is)"
  [resp f]
  (test/is (f resp))
  resp)
