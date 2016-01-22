(ns de.zalf.berest.client.hoplon.macro
  (:require [clojure.string :as str]))

(defn ns-kw->id [ns-keyword]
  (str (namespace ns-keyword) "-" (name ns-keyword)))

(defn id->ns-kw [id]
  (apply keyword (str/split id #"-" 2)))

(defmacro gen-state
  [state path & {:as kvs}]
  (let [path* (if (seq? path) path [path])
        res (reduce (fn [{:keys [code sub-state-map]} [k v]]

                      {:code (conj code
                                   `(def ~(-> k ns-kw->id symbol) (-> ~state ~@path* ~k))
                                   `(defn ~(symbol (str "set-" (-> k ns-kw->id name) "-fn"))
                                      ['value]
                                      (swap! ~state update-in [~@path* ~k] 'value)))
                       :sub-state-map (assoc sub-state-map k v)})
                    {:code [] :sub-state-map {}} kvs)]
    `(do
       ~@(:code res)
       ~(:sub-state-map res))))



