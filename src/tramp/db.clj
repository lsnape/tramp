(ns tramp.db
  (:require [monger.core :as monger]
            [monger.collection :as coll]
            [tramp.config :refer [config]])
  (:import org.bson.types.ObjectId))

(defn mongo-conn []
  (monger/connect (:mongo-db (config))))

(defn insert-listings! [listings]
  (when (seq listings)
    (let [conn (mongo-conn)
          {:keys [host port db-name coll]} (:mongo-db (config))
          db (monger/get-db (mongo-conn) db-name)]

      (map #(assoc % :_id (ObjectId.)) listings)
      (coll/insert-batch db coll listings))))

(defn seen-listing? [{:keys [listing-id] :as listing}]
  (let [{:keys [host port db-name coll]} (:mongo-db (config))
        db (monger/get-db (mongo-conn) db-name)]
    
    (coll/any? db coll {:listing-id listing-id})))
