(ns re-pl.core
  (:require
   cljsjs.codemirror
   cljsjs.codemirror.mode.clojure
   cljsjs.codemirror.keymap.emacs

   cljsjs.codemirror.addon.edit.closebrackets
   cljsjs.codemirror.addon.edit.matchbrackets
   datascript.core
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-pl.handlers]
   [re-pl.subs]
   [re-pl.views :as views]
   [re-pl.config :as config]))

(when config/debug?
  (println "dev mode"))

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:update-prompt!])
  (re-frame/dispatch [:console/prompt!])
  (mount-root))
