(ns tramp.main
  (:require [tramp.db :as db]
            [tramp.render :as render]
            [tramp.config :refer [config]]
            [tramp.fetch :refer [fetch-listings!]]
            [camel-snake-kebab.core :as csk]
            [cemerick.url :refer [url]]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [medley.core :refer [map-keys find-first assoc-some]]
            [net.cgrand.enlive-html :as html]
            [tramp.email :as email])
  (:import [tramp.gumtree GumtreeListingSource]))

(def fetch-jobs
  [{:site :gumtree
    :search-opts {:search-category "1-bedroom-rent"
                  :search-location "bristol"}}
   {:site :gumtree
    :search-opts {:search-category "2-bedrooms-rent"
                  :search-location "bristol"}}])

(defn fetch-new-listings! [{:keys [site search-opts] :as fetch-job}]
  (->> (fetch-listings! fetch-job)
       (remove db/seen-listing?)))

(defn start-watching! []
  (letfn [(fetch-listings! [time]
            (prn "fetching listings at" time)
            (let [{:keys [email]} (:email-creds (config))
                  new-listings (mapcat fetch-new-listings! fetch-jobs)]

              (when (seq new-listings)
                (email/send-html-email! {:from email
                                         :to email
                                         :subject "Today's listings"
                                         :body (render/render-listings-email new-listings)}))))]
    
    {:stop-fn (chime-at (periodic-seq (t/now) (t/minutes 1))
                fetch-listings!)}))

(defn -main [& _]
  (start-watching!))
