(ns re-pl.handlers
    (:require [re-frame.core :as re-frame]
              [re-pl.db :as db]
              [re-pl.repl :refer [get-prompt
                                  read-eval-call]]
              [clojure.string :as str]))

(re-frame/register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

;; repl prompt
(re-frame/register-handler
 :update-prompt!
 (fn [db _]
   (assoc db :prompt (get-prompt))))

;; handle eval result
(re-frame/register-handler
 :store-result
 (fn [{:keys [prompt] :as db} [_ {:keys [value form error warning] :as result}]]
   (re-frame/dispatch [:update-prompt!])
   (-> db
       ;; update raw results
       (update :results-data conj result)
       ;; update string buffer
       (update :buffer str (str
                            (str "\n" prompt form)
                            (when warning (str "\n" warning))
                            "\n"
                            (or value error))))))

;; eval a string
(re-frame/register-handler
 :read-eval-call
 (fn [db [_ input-str]]
   (read-eval-call
    #(re-frame/dispatch [:store-result %])
    input-str)
   db))
