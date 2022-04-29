(ns de.zalf.berest.client.hoplon.helper)

(defn thrush [& args]
  (reduce #(%2 %1) args))

(defn rcomp
  "reverse compose function, composes arguments from left to right
  (pipeline order)"
  [& args]
  (apply comp (reverse args)))

(defn partial-kw
  "partial function which works with keyword (optional) arguments"
  [f & kw-args]
  (fn [& args]
    (apply f (concat args kw-args))))

(defn ajuxt
  "juxts result directly applied to last argument, thus
  directly useable as a function with variable arguments
  (at least be two)"
  [& rest]
  ((apply juxt (butlast rest)) (last rest)))

(defn args-21->12
  "swap the two arguments before applying f to them"
  [f arg2 arg1]
  (f arg1 arg2))

(def swap
  "swap the two arguments before applying them to f"
  args-21->12)

(defn args-231->123
  "rotate arguments in the order described by the function's name"
  [f arg2 arg3 arg1]
  (f arg1 arg2 arg3))

(defn quote-regex
  [regex]
  (.replace regex (js/RegExp "([()[{*+.$^\\|?])" "g") "\\$1"))


