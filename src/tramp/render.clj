(ns tramp.render
  (:require [hiccup.core :refer [html]]))

(defn render-listings [listings]
  (html
   [:div.listings
    (for [{:keys [title description image-url url]} listings]
      [:div.listing
       [:h3 title]
       [:a {:href url}
        [:img {:src image-url}]]
       [:p description]])]))

(defn render-listings-email [listings]
  (html [:body
         (render-listings listings)]))

(comment
  (def foo-listings
    (-> (slurp "/tmp/listings.edn")
        read-string))
  
  (spit "/tmp/email.html" (render-listings-email foo-listings)))
