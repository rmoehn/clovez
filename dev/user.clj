(ns user
  (:require [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.repl :refer [pst doc find-doc]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clj-http.client :as http]
            [clj-slack.users]
            [com.rpl.specter :as s]

            ))



(comment

  (def connection {:api-url "https://slack.com/api"
                   :token (System/getenv "CLOVEZ_SLACK_TOKEN")})
  (def user-list (clj-slack.users/list connection))

  (->> user-list
       :members
       #_(filter #(= (:name %) "mr"))
       (filter #(:title (:profile %)))
       (map (fn [u] (merge u (:profile u))))
       (map (fn [u] (select-keys u [:name :real_name :tz :title])))
       pprint
       )

  )
