(ns user
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [pst doc find-doc]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clj-http.client :as http]
            [clj-slack.users]
            [com.rpl.specter :as s]
            [tentacles.users]
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

(defn twitter-urls [twitter-m]
  (s/select [:body :entities :url :urls s/ALL :expanded_url] twitter-m))

(defn add-twitter [slack-m]
  (if (seq (:twitter slack-m))
    slack-m
    (let [full (try (twitter/users-show :oauth-creds twitter-creds
                                        :params {:screen-name (:name slack-m)})
                    (catch Exception e nil))]
      (-> slack-m
          (assoc :twitter
                 (select-keys (:body full) [:screen_name :name :location
                                            :description]))
          (assoc-in [:twitter :urls] (twitter-urls full))))))

(defn add-github [slack-m]
  (if (seq (:github slack-m))
    slack-m
    (let [res (tentacles.users/user
                (:name slack-m)
                {:auth (str (System/getenv "CLOVEZ_GITHUB_USER")
                            \:
                            (System/getenv "CLOVEZ_GITHUB_TOKEN"))})]
      (if (:name res)
        (assoc slack-m :github
               (select-keys res [:name :email :location :blog :company
                                 :html_url :bio]))
        slack-m))))

(comment

  (def user-list (clj-slack.users/list slack-connection))

  (->> user-list
       :members
       #_(filter #(= (:name %) "x"))
       #_(filter #(:title (:profile %)))
       (map (fn [u] (merge u (:profile u))))
       (map (fn [u] (select-keys u [:name :real_name :tz :title])))
       (map add-twitter)
       (map add-github)
       (into [])
       (spit "att-info.edn")
       )

  (pprint (clojure.edn/read-string (slurp "att-info.edn")))


  (pprint (twitter-urls (twitter/users-show :oauth-creds twitter-creds :params {:screen-name "favila"})))

  (http/get "https://api.twitter.com/1.1/users/show.json"
            {:query-params {:name "delaybeforesend"}}
            )

  (http/get "https://api.github.com/users/rmoehn")

  (pprint (tentacles.users/user
            "rmoehn"
            {:auth (str (System/getenv "CLOVEZ_GITHUB_USER")
                        \:
                        (System/getenv "CLOVEZ_GITHUB_TOKEN"))}))

  )
