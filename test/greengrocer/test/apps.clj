(ns greengrocer.test.apps
  "Some ring apps for testing against."
  (:use hiccup.core)
  (:use compojure.core)
  (:use sandbar.stateful-session)
  (:require [ring.util.response :as response]
            [compojure.handler :as handler]))

(defroutes session-app-routes
  (GET "/" []
    (html
      [:ul
       (for [item (session-get :items)]
         [:li item])]
      [:form {:id "item-form" :method "post"}
        [:input {:type "text" :name "item"}]
        [:input {:type "submit" :value "yeah"}]]))
  (POST "/" [item]
    (session-put! :items (conj (session-get :items) item))
    (response/redirect "/")))

(def session-app
  (->
    session-app-routes
    (handler/site)
    (wrap-stateful-session)))
