(ns re-pl.handlers
    (:require [re-frame.core :as re-frame]
              [re-pl.db :as db]
              [re-pl.repl :refer [get-prompt
                                  read-eval-call]]
              [re-pl.console :refer [get-input
                                     set-input
                                     clear-marks!
                                     mark-buffer]]
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
 (fn [{:keys [prompt console] :as db} [_ {:keys [value form error warning] :as result}]]

   (when console
     (when warning
       (re-frame/dispatch [:console/write (str "\n" warning)]))
     (re-frame/dispatch [:console/write (str "\n" (or value error))])
     (re-frame/dispatch
      [:console/prompt!]))

   (-> db
       (assoc :prompt (get-prompt))
       (update :results-data conj result))))

;; eval a string
(re-frame/register-handler
 :read-eval-call
 (fn [db [_ input-str]]
   (read-eval-call
    #(re-frame/dispatch [:store-result %])
    input-str)
   (-> db
       (assoc :state :eval)
       (update :history conj input-str)
       (assoc :history-pos 0))))




;; codemirror console

(re-frame/register-handler
 :console/init
 (fn [db [_ el opts]]
   (let [cmi (:console db)]
     (if cmi
       db
       (let [cm (.fromTextArea
                 js/CodeMirror
                 el
                 (clj->js
                  (merge
                   {:mode "clojure"
                    :keyMap "emacs"
                    :theme "monokai"
                    :matchBrackets true
                    :autoCloseBrackets true
                    :lineNumbers false
                    :extraKeys {"Enter"
                                #(re-frame/dispatch [:console/read-prompt])
                                "Ctrl-Up" #(re-frame/dispatch [:history/prev])
                                "Ctrl-Down" #(re-frame/dispatch [:history/next])}}
                   opts)))]
         (do
           (set! *print-newline* false)
           (set! *print-fn*
                 (fn [& args]
                   (re-frame/dispatch [:console/print (apply str args)])))
           (set! *print-err-fn*
                 (fn [& args]
                   (re-frame/dispatch [:console/print (apply str args)])))
           )
         (assoc db :console cm))))))


(re-frame/register-handler
 :console/read-prompt
 (fn [{:keys [prompt] :as db} _]
   (let [cm (:console db)]
     (re-frame/dispatch
      [:read-eval-call
       (get-input cm)])
     db)))


(re-frame/register-handler
 :console/write
 (fn [{:keys [console] :as db} [_ text]]
   (if console
     (let [last-line (.lastLine console)]
       (.replaceRange console
                      text
                      #js {:line (inc last-line)
                           :ch 0})
       db)
     (throw (js/Error. "No console!")))))

;
(re-frame/register-handler
 :console/print
 (fn [{:keys [console state] :as db} [_ message]]
   (when console
     (let [last-line (.lastLine console)]
       (.replaceRange console
                      (str "\n" message)
                      #js {:line (inc last-line)
                           :ch 0})
       (when (#{:init :input} state)
         (re-frame/dispatch [:console/prompt!]))))
   db))

(re-frame/register-handler
 :console/prompt!
 (fn [{:keys [console prompt state] :as db} [_ ?force-newline]]
   (if (and console prompt)
     (let [last-line (.lastLine console)
           prompt-line (inc last-line)]
       (doto console
         ;; clear all doc marks
         clear-marks!

         ;; if there is a buffer, make it read-only
         ;; (cond-> new-line? mark-buffer)
         mark-buffer

         ;; add the prompt
         (.replaceRange
          (str
           "\n"
           prompt)
          #js {:line last-line}) ;; no ch means EOL

         ;; mark prompt
         (.markText
          #js {:line prompt-line
               :ch 0}
          #js {:line prompt-line
               :ch (count prompt)}
          #js {:className "re-pl-prompt"
               :readOnly true})

         ;; mark input, this will be the last mark
         (.markText
          #js {:line prompt-line
               :ch (inc (count prompt))}
          #js {:line prompt-line}
          #js {:className "re-pl-input"
               :clearWhenEmpty false
               :inclusiveLeft true
               :inclusiveRight true})

         ;; set the cursor to eol
         (.setCursor #js {:line prompt-line}))

       ;; set the app state to input
       (assoc db :state :input))
     (do
       ;; redispatch until app is ready
       (re-frame/dispatch
        [:console/prompt!])
       db))))


;; history

(re-frame/register-handler
 :history/prev
 (fn [{:keys [history history-pos console] :as db} _]
   (if (> (count history) history-pos)
     (let [current-input (get-input console)
           history-item (nth history history-pos)]

       (set-input console history-item)

       (cond-> (update db :history-pos inc)
         (= 0 history-pos) (assoc :history-swap current-input)))
     db)))

(re-frame/register-handler
 :history/next
 (fn [{:keys [history history-pos console history-swap] :as db} _]
   (if (= 0 history-pos)
     db
     (do
       (set-input console
                  (if (= 0 (dec history-pos))
                    history-swap
                    (nth history (- history-pos 2))))
       (update db :history-pos dec)))))
