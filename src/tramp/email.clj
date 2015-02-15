(ns tramp.email
  (:require [postal.core :as postal]
            [tramp.config :refer [config]]))

(defn email-creds []
  (assoc (:email-creds (config)) :ssl :yes!!!11))

(defn send-html-email! [{:keys [from to subject body]}]
  (postal/send-message (email-creds) {:from "ldjsnape@gmail.com"
                                      :to "ldjsnape@gmail.com"
                                      :subject subject
                                      :body [{:type "text/html"
                                              :content body}]}))

(comment
  (send-html-email! {:from "ldjsnape@gmail.com"
                     :to "ldjsnape@gmail.com"
                     :subject "Today's listings"
                     :body (slurp "/tmp/email.html")}))
