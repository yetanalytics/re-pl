(ns re-pl.console)


(defn new-console
  "return a new codemirror editor"
  [textarea clj-opts]
  (.fromTextArea
   js/CodeMirror
   textarea
   (clj->js
    clj-opts)))

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
