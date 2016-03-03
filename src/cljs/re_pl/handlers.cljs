(ns re-pl.handlers
    (:require [re-frame.core :as re-frame]
              [re-pl.db :as db]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))
