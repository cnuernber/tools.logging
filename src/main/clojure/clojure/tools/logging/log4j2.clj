(ns clojure.tools.logging.log4j2
  (:require [clojure.tools.logging.impl :as i]))


(defn init []
  (let [; Same as is done inside LogManager/getLogger(String).
        context (org.apache.logging.log4j.LogManager/getContext false)
        levels {:trace org.apache.logging.log4j.Level/TRACE
                 :debug org.apache.logging.log4j.Level/DEBUG
                 :info  org.apache.logging.log4j.Level/INFO
                 :warn  org.apache.logging.log4j.Level/WARN
                 :error org.apache.logging.log4j.Level/ERROR
                 :fatal org.apache.logging.log4j.Level/FATAL}]
    (extend org.apache.logging.log4j.Logger
      i/Logger
      {:enabled?
       (fn [logger level]
         (.isEnabled ^org.apache.logging.log4j.Logger logger
                     ^org.apache.logging.log4j.Level  (get levels level level)))
       :write!
       (fn [^org.apache.logging.log4j.Logger logger level e msg]
         (let [level (get levels level level)]
           (if e
             (.log ^org.apache.logging.log4j.Logger logger
                   ^org.apache.logging.log4j.Level  level
                   ^Object                          msg
                   ^Throwable                       e)
             (.log ^org.apache.logging.log4j.Logger logger
                   ^org.apache.logging.log4j.Level  level
                   ^Object                          msg))))})
    (reify i/LoggerFactory
      (name [_]
        "org.apache.logging.log4j")
      (get-logger [_ logger-ns]
        (.getLogger context ^String (str logger-ns))))))
