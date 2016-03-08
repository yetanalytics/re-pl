(ns re-pl.console)

(defn find-mark-range
  "find a CodeMirror marked range by a single string classname."
  [cm mark-class]
  (let [js-range (some
                  (fn [m]
                    (when (= mark-class (.-className m))
                      (.find m)))
                  (.getAllMarks cm))

        from-line (-> js-range .-from .-line)
        from-ch (-> js-range .-from .-ch)
        to-line (-> js-range .-to .-line)
        to-ch (-> js-range .-to .-ch)]
    {:from {:line from-line
            :ch from-ch}
     :to {:line to-line
          :ch to-ch}}))

(defn clear-marks! [cm]
  (doseq [mark (.getAllMarks cm)]
    (.clear mark))
  cm)

(defn mark-text
  "Mark a CodeMirror range with the given options"
  [cm {:keys [from to options]}]
  (doto cm
    (.markText (clj->js from)
               (clj->js to)
               (clj->js options))))

(defn set-cursor
  "Set the CodeMirror cursor position"
  [cm pos]
  (doto cm
      (.setCursor (clj->js pos))))


;; api

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
    (doto cm
      (.replaceRange
       (str
        (when newline? "\n")
        s)
       #js {:line last-line}))))


(defn get-input
  "get the repl user input"
  [cm]
  (let [{:keys [from to]} (find-mark-range cm "re-pl-input")]
    (.getRange
     cm
     (clj->js from) (clj->js to))))

(defn set-input
  "replace the repl user input"
  [cm input-str]
  (let [{:keys [from to]} (find-mark-range cm "re-pl-input")]
    (.replaceRange
     cm input-str
     (clj->js from) (clj->js to))))




(defn reprompt
  "Regroup all text into a read-only marker,
   and prompt the user"
  [cm prompt]
  (.operation
   cm
   #(let [last-line (.lastLine cm)
          prompt-line (inc last-line)]
      (-> cm
        ;; clear all doc marks
        clear-marks!

        ;; mark buffer, read-only
        (mark-text
         {:from {:line 0
                 :ch 0}
          :to {:line last-line}
          :options {:className "re-pl-buffer"
                    :readOnly true}})

        ;; add the prompt
        (append prompt true)

        ;; mark prompt
        (mark-text
         {:from {:line prompt-line
                 :ch 0}
          :to {:line prompt-line
               :ch (count prompt)}
          :options {:className "re-pl-prompt"
                    :readOnly true}})

        ;; mark input, this will be the last mark
        (mark-text
         {:from {:line prompt-line
                 :ch (inc (count prompt))}
          :to {:line prompt-line}
          :options {:className "re-pl-input"
                    :clearWhenEmpty false
                    :inclusiveLeft true
                    :inclusiveRight true}})

        ;; set the cursor to eol
        (set-cursor {:line prompt-line})))))
