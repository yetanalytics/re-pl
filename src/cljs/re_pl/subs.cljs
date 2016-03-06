(ns re-pl.subs
    (:require-macros [reagent.ratom :refer [reaction]])
    (:require [re-frame.core :as re-frame]))


(re-frame/register-sub
 :results
 (fn [db]
   (reaction (:results-data @db))))


(re-frame/register-sub
 :debug/dump
 (fn [db]
   (reaction @db)))
