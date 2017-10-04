(ns re-pl.handlers
    (:require [re-frame.core :as re-frame]
              [re-pl.db :as db]
              [re-pl.repl :refer [get-prompt
                                  read-eval-call
                                  autocomplete-terms]]
              [re-pl.console :refer [new-console
                                     append
                                     reprompt
                                     get-input
                                     set-input
                                     clear-marks!
                                     show-hint]]
              [clojure.string :as str]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

;; repl prompt
(re-frame/reg-event-db
 :update-prompt!
 (fn [db _]
   (assoc db :prompt (get-prompt))))

;; handle eval result
(re-frame/reg-event-db
 :store-result
 (fn [db [_ {:keys [value form error warning] :as result}]]

   (re-frame/dispatch
    [:console/write-result])

   (-> db
       (assoc :prompt (get-prompt))
       (update :results-data conj result))))


;; codemirror console

(re-frame/reg-event-db
 :console/init
 (fn [db [_ el opts]]
   (let [cmi (:console db)]
     (if cmi
       db
       (let [cm (new-console
                 el
                 (merge
                  {:autofocus true
                   :mode "clojure"
                   :keyMap "emacs"
                   :theme "monokai"
                   :matchBrackets true
                   :autoCloseBrackets true
                   :lineNumbers false
                   :lineWrapping true
                   :extraKeys {"Enter"
                               #(re-frame/dispatch [:console/read-prompt])
                               "Ctrl-Up" #(re-frame/dispatch [:history/prev])
                               "Ctrl-Down" #(re-frame/dispatch [:history/next])
                               "Tab" #(re-frame/dispatch [:console/hint])}}
                  opts))]
         ;; switch printing to repl
         (do
           (set! *print-newline* false)
           (set! *print-fn*
                 (fn [& args]
                   (re-frame/dispatch [:console/print (apply str args)])))
           (set! *print-err-fn*
                 (fn [& args]
                   (re-frame/dispatch [:console/print (apply str args)]))))
         (assoc db :console cm))))))


(re-frame/reg-event-db
 :console/read-prompt
 (fn [{:keys [prompt console] :as db} _]
   (let [input-str (str (get-input console))]
     ;; eval the form
     (read-eval-call
      #(re-frame/dispatch [:store-result %])
      input-str)

     (-> db
         (assoc :state :eval)
         (update :history conj input-str)
         (assoc :history-pos 0)))))


(re-frame/reg-event-db
 :console/write-result
 (fn [{:keys [console results-data] :as db} _]
   (let [{:keys [value form error warning] :as result} (peek results-data)]

     (.operation
      console
      #(doto console
         (cond-> warning (append (str warning) true))
         (append (str (or value error)) true)))

     (re-frame/dispatch [:console/prompt!])

     db)))


(re-frame/reg-event-db
 :console/print
 (fn [{:keys [console state] :as db} [_ message]]
   (when console
     (append console message true)
     (when (#{:init :input} state)
       (re-frame/dispatch [:console/prompt!])))
   db))


(re-frame/reg-event-db
 :console/prompt!
 (fn [{:keys [console prompt state] :as db} _]
   (if (and console prompt)
     (do
       (reprompt console prompt)

       ;; set the app state to input
       (assoc db :state :input))
     (do
       ;; redispatch until app is ready
       (re-frame/dispatch
        [:console/prompt!])
       db))))

(re-frame/reg-event-db
 :console/hint
 (fn [{:keys [history] :as db} _]
   (let [cm (:console db)](show-hint
    cm
    {:hint (fn [cm opts]

             (-> js/CodeMirror
                 .-hint
                 (.fromList cm
                            (clj->js
                             (merge (js->clj opts)
                                    {:words (autocomplete-terms)})))))})
   db)))

;; history

(re-frame/reg-event-db
 :history/prev
 (fn [{:keys [state history history-pos console] :as db} _]
   (if (and (= state :input)
           (> (count history) history-pos))
     (let [current-input (get-input console)
           history-item (nth history history-pos)]

       (set-input console history-item)

       (cond-> (update db :history-pos inc)
         (= 0 history-pos) (assoc :history-swap current-input)))
     db)))

(re-frame/reg-event-db
 :history/next
 (fn [{:keys [state history history-pos console history-swap] :as db} _]
   (if (or (= state :eval)
           (= 0 history-pos))
     db
     (do
       (set-input console
                  (if (= 0 (dec history-pos))
                    history-swap
                    (nth history (- history-pos 2))))
       (update db :history-pos dec)))))
