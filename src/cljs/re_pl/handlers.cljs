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
     (when warning (re-frame/dispatch [:console/write-line (str warning)]))
     (re-frame/dispatch [:console/write-line (str (or value error))])
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
       ;; update the buffer count if there are extra line breaks
       (update :buffer-lines + (-> input-str
                                   str/split-lines
                                   count
                                   dec)))))


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
                    :extraKeys {:Enter
                                #(re-frame/dispatch [:console/read-prompt])}}
                   opts)))]
         (assoc db :console cm))))))

(re-frame/register-handler
 :console/read-prompt
 (fn [{:keys [buffer-lines prompt] :as db} _]
   (let [cm (:console db)]
     (let [input-range-js (.find (last (.getAllMarks cm)))

           from #js {:line (-> input-range-js .-from .-line)
                     :ch (-> input-range-js .-from .-ch)}

           to #js {:line (-> input-range-js .-to .-line)
                   :ch (-> input-range-js .-to .-ch)}

           input (.getRange cm from to)

           cpos (.getCursor cm)
           c-line (.-line cpos)]
       (if (< (dec buffer-lines) c-line)
         (do
           (re-frame/dispatch
            [:read-eval-call
             input])
           db)
         db)))))


(re-frame/register-handler
 :console/write-line
 (fn [{:keys [console] :as db} [_ line]]
   (if console
     (let [line-count (.lineCount console)]
       (.replaceRange console
                      (str "\n" line)
                      #js {:line line-count
                           :ch 0})
       (update db :buffer-lines inc))
     (throw (js/Error. "No console!")))))



(defn clear-marks! [cm]
  (doseq [mark (.getAllMarks cm)]
    (.clear mark)))

(defn mark-buffer [cm]
  (let [last-line (.lastLine cm)]
    (doto cm
        (.markText
         #js {:line 0
              :ch 0}
         #js {:line last-line}
         #js {:className "re-pl-buffer"
              :readOnly true}))))

(re-frame/register-handler
 :console/prompt!
 (fn [{:keys [console prompt state] :as db} _]
   (if (and console prompt)
     (let [last-line (.lastLine console)
           line-count (.lineCount console)
           after-eval? (= :eval state) ;; will be true if this isn't the first prompt
           ;; escaped-prompt (str prompt space)
           ]
       (doto console
         ;; clear all doc marks
         clear-marks!

         ;; if there is a buffer, make it read-only
         (cond-> after-eval? mark-buffer)

         ;; add the prompt
         (.replaceRange
          (str
           (when after-eval?
             "\n")
           prompt)
          #js {:line last-line}) ;; no ch means EOL

         ;; mark prompt
         (.markText
          #js {:line (cond-> last-line
                       after-eval? inc)
               :ch 0}
          #js {:line (cond-> last-line
                       after-eval? inc)
               :ch (count prompt)}
          #js {:className "re-pl-prompt"
               :readOnly true})

         ;; mark input, this will be the last mark
         (.markText
          #js {:line (cond-> last-line
                       after-eval? inc)
               :ch (inc (count prompt))}
          #js {:line (inc (cond-> last-line
                       after-eval? inc))}
          #js {:className "re-pl-input"
               :clearWhenEmpty false
               :inclusiveLeft true
               :inclusiveRight true})

         ;; set the cursor to eol
         (.setCursor #js {:line (cond-> last-line
                                  after-eval? inc)}))

       ;; set the app state to input, also
       ;; tell the app about new lines if we have them
       (cond-> (assoc db :state :input)
         (= :eval state)
         (update :buffer-lines inc)))
     (do
       ;; redispatch until app is ready
       (re-frame/dispatch
        [:console/prompt!])
       db))))
