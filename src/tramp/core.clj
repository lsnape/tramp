(ns tramp.core
  (:require [tramp.db :as db]
            [tramp.render :as render]
            [camel-snake-kebab.core :as csk]
            [cemerick.url :refer [url]]
            [chime :refer [chime-at]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [medley.core :refer [map-keys find-first assoc-some]]
            [net.cgrand.enlive-html :as html]
            [tramp.email :as email]))

(defn gumtree-base-url []
  "http://www.gumtree.com")

(defn gumtree-request [{:keys [search-category search-location distance] :as query-params}]
  (let [search-url (str (-> (url (gumtree-base-url) "search")
                            (assoc :query (map-keys csk/->snake_case_keyword {:search-category search-category
                                                                              :search-location search-location}))))]
    (html/html-resource (java.net.URL. search-url))))

;; TODO - use zippers here :)
(defn parse-image-elem [{:keys [content] :as image-elem}]
  (->> content
       (map :content)
       (remove nil?)
       flatten
       (map :attrs)
       (find-first :src)
       :src))

(defn parse-search-html [search-html]
  (for [article (html/select search-html [:ul.primary-listings :article])

        :let [[image-url] (->> (html/select article [:.listing-thumbnail])
                               (map parse-image-elem))]]

    (-> {:listing-id (str "gumtree-" (get-in article [:attrs :id]))
     
         :title (->> (html/select article [:div.listing-content])
                     (map (comp :content second :content))
                     flatten)

         :description (->> (html/select article [:div.listing-content])
                           (map (comp #(nth % 3) :content))
                           first
                           :content
                           flatten)}
        
        (assoc-some :image-url image-url))))

(defn fetch-gumtree-listings! [{:keys [search-category search-location distance] :as opts}]
  (->> (gumtree-request opts)
       parse-search-html
       (remove db/seen-listing?)))

(defn start-watching! []
  (letfn [(fetch-listings! [time]
            (prn "fetching listings at" time)
            (for [[search-category search-location] [["1-bedroom-rent" "bristol"]
                                                     ["2-bedrooms-rent" "bristol"]]]

              (-> (fetch-gumtree-listings! {:search-category search-category
                                            :search-location search-location})
                              
                  render/render-listings-email

                  (as-> email-body
                    (email/send-html-email! {:from "ldjsnape@gmail.com"
                                             :to "ldjsnape@gmail.com"
                                             :subject "Today's listings"
                                             :body email-body})))))]
    {:stop-fn (chime-at (periodic-seq (t/now) (t/minutes 1))
                fetch-listings!)}))

(defn -main [& _]
  (start-watching!))

(comment
  (-> (fetch-gumtree-listings! {:search-category "1-bedroom-rent"
                                :search-location "bristol"})

      render/render-listings-email

      (as-> email-body
        (email/send-html-email! {:from "ldjsnape@gmail.com"
                                 :to "ldjsnape@gmail.com"
                                 :subject "Today's listings"
                                 :body email-body})))
  
  )
