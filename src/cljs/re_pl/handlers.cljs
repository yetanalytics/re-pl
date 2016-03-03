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
 (fn [db [_ {:keys [value form error warning] :as result}]]
   (-> db
       ;; update raw results
       (update :results-data conj result)
       ;; update string buffer
       (update :buffer str (str
                            (when warning (str warning "\n"))
                            (or value error) "\n"))
       ;; reset hist position
       (assoc :history-position 0)
       )))

;; eval a string
(re-frame/register-handler
 :read-eval-call
 (fn [db [_ input-str]]
   (read-eval-call
    #(re-frame/dispatch [:store-result %])
    input-str)
   db))


;; repl input scratch
(re-frame/register-handler
 :set-scratch
 (fn [db [_ input-str]]
   (assoc db :scratch input-str)))

;; History

(re-frame/register-handler
 :history/get
 (fn [{:keys [history-position results-data scratch] :as db} [_ el]]
   (aset el
         "textContent"
         (if (< 0 history-position)
           (-> results-data
               (nth (dec history-position))
               :form
               str)
           scratch))
   db))

(re-frame/register-handler
 :history/prev
 (fn [{:keys [results-data history-position] :as db} [_ el]]
   (let [prev (inc history-position)]
     (if (<= prev (count results-data))
       (do
        (re-frame/dispatch [:history/get el])
        (assoc db :history-position prev))
       db))))

(re-frame/register-handler
 :history/next
 (fn [{:keys [results-data history-position] :as db} [_ el]]
   (let [n (dec history-position)]
     (if (<= 0 n)
       (do
         (re-frame/dispatch [:history/get el])
         (assoc db :history-position n))
       db))))
