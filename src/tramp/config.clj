(ns tramp.config
  (:require [clojure.java.io :as io]
            [nomad :refer [defconfig]]))

(defconfig config
  (io/resource "tramp-config.edn"))
