(ns clojure.tools.logging.log4j
  (:require [clojure.tools.logging.impl :as i]))


(defn init []
  (let [levels {:trace org.apache.log4j.Level/TRACE
                 :debug org.apache.log4j.Level/DEBUG
                 :info  org.apache.log4j.Level/INFO
                 :warn  org.apache.log4j.Level/WARN
                 :error org.apache.log4j.Level/ERROR
                 :fatal org.apache.log4j.Level/FATAL}]
    (extend org.apache.log4j.Logger
      i/Logger
      {:enabled?
       (fn [^org.apache.log4j.Logger logger level]
         (.isEnabledFor logger (get levels level level)))
       :write!
       (fn [^org.apache.log4j.Logger logger level e msg]
         (let [level (get levels level level)]
           (if e
             (.log logger level msg e)
             (.log logger level msg))))})
    (reify i/LoggerFactory
      (name [_]
        "org.apache.log4j")
      (get-logger [_ logger-ns]
        (org.apache.log4j.Logger/getLogger ^String (str logger-ns))))))
