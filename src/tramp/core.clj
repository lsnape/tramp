(ns tramp.core
  (:require [cemerick.url :refer [url]]
            [net.cgrand.enlive-html :as html]
            [clj-http.client :as http]
            [camel-snake-kebab.core :as csk]
            [medley.core :refer [map-keys]]))

(defn gumtree-base-url []
  "http://www.gumtree.com")

(defn gumtree-request [{:keys [search-category search-location distance] :as query-params}]
  (let [search-url (str (-> (url (gumtree-base-url) "search")
                            (assoc :query {:search-category search-category
                                           :search-location search-location})))]
    
    (html/html-resource (java.net.URL. search-url))))

(comment
  ;; primary-listings
  (->> (gumtree-request {:search-category "1-bedroom-rent"
                         :search-location "bristol"})
       
       (spit "/tmp/gt.html"))
  
  (def foo-req
    (gumtree-request {:search-category "1-bedroom-rent"
                      :search-location "bristol"
                      :distance "0.0001"}))


  ;; TODO - get the title, picture, and URL of the listing. That's all
  (html/select foo-req [:ul.primary-listings]))
