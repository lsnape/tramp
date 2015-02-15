(ns tramp.recur
  (:require [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [chime :refer [chime-at]]))

(defmacro at-minute-interval [minutes & body]
  `('~chime-at '~(periodic-seq (t/now) (t/minutes minutes))
               ~@body))

(comment
  (macroexpand-1 '(at-minute-interval 1 (prn "kook")))

  
  (def stop-fn
    (watch-listings!))

  (stop-fn))

