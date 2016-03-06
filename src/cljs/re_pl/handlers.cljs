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
 (fn [{:keys [prompt console] :as db} [_ {:keys [value form error warning] :as result}]]

   (when console
     ;; (re-frame/dispatch [:console/write-line (str "\n" prompt form)])
     (when warning (re-frame/dispatch [:console/write-line (str "\n" warning)]))
     (re-frame/dispatch [:console/write-line (str "\n" (or value error))])
     (re-frame/dispatch [:console/write-line "\n"])
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
   db))


;; codemirror console


(defn read-prompt [cm]
  (let [prompt (get-prompt)
        last-line (.lastLine cm)
        ll-content (.getLine cm last-line)
        cpos (.getCursor cm)
        c-line (.-line cpos)
        c-ch (.-ch cpos)]
    (when (= last-line c-line)
      (re-frame/dispatch
       [:read-eval-call
        (apply str
             (drop (count prompt)
                   ll-content))]))))

(re-frame/register-handler
 :console/init
 (fn [{:keys [console] :as db} [_ el opts]]
   (if console
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
                  :extraKeys #js {:Enter read-prompt}}
                 opts)))
           console cm]
       (assoc db :console console)))))

(re-frame/register-handler
 :console/write-line
 (fn [{:keys [console] :as db} [_ line]]
   (if console
     (let [line-count (.lineCount console)
           ]
       (.replaceRange console
                      (str line)
                      #js {:line line-count
                           :ch 0})
       db)
     (throw (js/Error. "No console!")))
   db))

(re-frame/register-handler
 :console/prompt!
 (fn [{:keys [console prompt] :as db} _]
   (if (and console prompt)
     (let [line-count (.lineCount console)]
       (doto console
           (.replaceRange
                      (str prompt)
                      #js {:line line-count
                           :ch 0})
           (.setCursor #js {:line (dec line-count)
                            :ch (count prompt)}))
       db)
     (re-frame/dispatch
      [:console/prompt!]))
   db))
