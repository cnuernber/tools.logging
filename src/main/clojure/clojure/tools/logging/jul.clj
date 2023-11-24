(ns clojure.tools.logging.jul
  (:require [clojure.tools.logging.impl :as i]))


(defn init []
  (let [levels {:trace java.util.logging.Level/FINEST
                 :debug java.util.logging.Level/FINE
                 :info  java.util.logging.Level/INFO
                 :warn  java.util.logging.Level/WARNING
                 :error java.util.logging.Level/SEVERE
                 :fatal java.util.logging.Level/SEVERE}]
    (extend java.util.logging.Logger
      i/Logger
      {:enabled?
       (fn [^java.util.logging.Logger logger level]
         (.isLoggable logger (get levels level level)))
       :write!
       (fn [^java.util.logging.Logger logger level ^Throwable e msg]
         (let [^java.util.logging.Level level (get levels level level)
               ^String msg (str msg)]
           (if e
             (.log logger level msg e)
             (.log logger level msg))))})
    (reify i/LoggerFactory
      (name [_]
        "java.util.logging")
      (get-logger [_ logger-ns]
        (java.util.logging.Logger/getLogger (str logger-ns))))))
