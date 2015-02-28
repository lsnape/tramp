(ns tramp.fetch
  (:require [tramp.db :as db]
            [tramp.gumtree :as gumtree]
            [cemerick.url :refer [url]]
            [net.cgrand.enlive-html :as html]))

(defmulti fetch-listings! :site)

(defmethod fetch-listings! :gumtree [{:keys [search-opts]}]
  (gumtree/fetch-listings! search-opts))
