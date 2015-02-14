(ns tramp.core
  (:require [cemerick.url :refer [url]]
            [net.cgrand.enlive-html :as html]
            [clj-http.client :as http]
            [camel-snake-kebab.core :as csk]
            [medley.core :refer [map-keys find-first assoc-some]]))

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

    (-> {:id (get-in article [:attrs :id])
     
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
       (filter :image-url)
              
       ;; TODO - persistence layer + diffing
       ))

(comment
  (def foo-req
    (fetch-gumtree-listings! {:search-category "2-bedrooms-rent"
                              :search-location "bristol"}))
  )
