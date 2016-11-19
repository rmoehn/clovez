(ns user
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [pst doc find-doc]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clj-http.client :as http]
            [clj-slack.users]
            [com.rpl.specter :as s]
            [twitter.oauth]
            [twitter.api.restful :as twitter]
            ))


(def slack-connection {:api-url "https://slack.com/api"
                       :token (System/getenv "CLOVEZ_SLACK_TOKEN")})

(def twitter-creds (twitter.oauth/make-oauth-creds
                      (System/getenv "CLOVEZ_TWITTER_CONSUMER_KEY")
                      (System/getenv "CLOVEZ_TWITTER_CONSUMER_SECRET")
                      (System/getenv "CLOVEZ_TWITTER_ACCESS_TOKEN")
                      (System/getenv "CLOVEZ_TWITTER_ACCESS_TOKEN_SECRET")))

(defn add-twitter [slack-u]
  (if (:twitter slack-u)
    slack-u
    (let [full (try (twitter/users-show :oauth-creds twitter-creds
                                        :params {:screen-name (:name slack-u)})
                    (catch Exception e nil))]
      (assoc slack-u :twitter
             (select-keys (:body full) [:screen_name :name :location :description
                                :url])))))

(comment

  (def user-list (clj-slack.users/list slack-connection))

  (->> user-list
       :members
       #_(filter #(= (:name %) "AdamJWynne"))
       (filter #(:title (:profile %)))
       (map (fn [u] (merge u (:profile u))))
       (map (fn [u] (select-keys u [:name :real_name :tz :title])))
       (map add-twitter)
       (pprint)
       )


  (pprint (twitter/users-show :oauth-creds twitter-creds :params {:screen-name "delaybeforesend"}))

  (http/get "https://api.twitter.com/1.1/users/show.json"
            {:query-params {:name "delaybeforesend"}}
            )

  (http/get "https://api.github.com/users/rmoehn")

  )
