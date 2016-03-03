(ns re-pl.handlers
    (:require [re-frame.core :as re-frame]
              [re-pl.db :as db]
              [re-pl.repl :refer [read-eval-call]]
              [clojure.string :as str]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))


(re-frame/register-handler
 :store-result
 (fn [db [_ {:keys [value form error warning] :as result}]]
   (-> db
       ;; update raw results
       (update :results-data conj result)
       ;; update string buffer
       (update :buffer str (str (or value error) "\n")))))

(re-frame/register-handler
 :read-eval-call
 (fn [db [_ input-str]]
   (read-eval-call
    #(re-frame/dispatch [:store-result %])
    input-str)
   db))
