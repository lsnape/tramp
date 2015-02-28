(ns tramp.gumtree
  (:require [cemerick.url :refer [url]]
            [net.cgrand.enlive-html :as html]))

(def ^:dynamic *gumtree-base-url*
  "http://www.gumtree.com")

(def ^:dynamic *article-selector*
  [:ul.primary-listings :article])

(def ^:dynamic *title-selector*
  [:h2.listing-title])

(defn gumtree-url [{:keys [search-category search-location] :as query-params}]
  (str (-> (url *gumtree-base-url* "search")
           (assoc :query {:search_category search-category
                          :search_location search-location}))))

(defn extract [node]
  {:listing-id (get-in node [:attrs :id])
   
   :title (-> (html/select node *title-selector*)
              first :content first)
   
   :description (->> (html/select node [:p.listing-description])
                     (map :content)
                     ffirst)

   :url (->> (html/select node [:a])
             (map (comp #(str *gumtree-base-url* %) :href :attrs))
             first)

   :img-url (->> (html/select node [:img.hide-fully-no-js])
                 (map (comp first #(html/attr-values % :data-lazy)))
                 first)})

(defn fetch-gumtree-url! [query-params]
  (let [search-url (gumtree-url query-params)]
    (html/html-resource (java.net.URL. search-url))))

(defn fetch-listings! [query-params]
  (let [listing-nodes (-> (fetch-gumtree-url! query-params)
                          (html/select *article-selector*))]
    
    (map extract listing-nodes)))
