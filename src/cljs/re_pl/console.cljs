(ns re-pl.console)


(defn new-console
  "return a new codemirror editor"
  [textarea clj-opts]
  (.fromTextArea
   js/CodeMirror
   textarea
   (clj->js
    clj-opts)))


(defn append
  "Append a string to the console"
  [cm s & [newline?]]
  (let [last-line (.lastLine cm)]
    (.replaceRange cm
                   (str
                    (when newline? "\n")
                    s)
                   #js {:line last-line})))


(defn get-input [cm]
  (let [input-range-js
        (some
         (fn [m]
           (when (= "re-pl-input" (.-className m))
             (.find m)))
         (.getAllMarks cm))

        from #js {:line (-> input-range-js .-from .-line)
                  :ch (-> input-range-js .-from .-ch)}

        to #js {:line (-> input-range-js .-to .-line)
                :ch (-> input-range-js .-to .-ch)}

        input (.getRange cm from to)]
    input))

(defn set-input [cm input-str]
  (let [input-range-js
        (some
         (fn [m]
           (when (= "re-pl-input" (.-className m))
             (.find m)))
         (.getAllMarks cm))

        from #js {:line (-> input-range-js .-from .-line)
                  :ch (-> input-range-js .-from .-ch)}

        to #js {:line (-> input-range-js .-to .-line)
                :ch (-> input-range-js .-to .-ch)}]
    (.replaceRange cm input-str from to)))

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



(defn reprompt
  "Regroup all text into a read-only marker,
   and prompt the user"
  [cm prompt]
  (.operation
   cm
   #(let [last-line (.lastLine cm)
          prompt-line (inc last-line)]
      (doto cm
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
        (.setCursor #js {:line prompt-line})))))
