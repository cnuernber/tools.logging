(ns clojure.tools.logging.slf4j
  (:require [clojure.tools.logging.impl :as i]))


(defn init []
  (let [                         ; Same as is done inside LoggerFactory/getLogger(String).
        factory (org.slf4j.LoggerFactory/getILoggerFactory)]
    (extend org.slf4j.Logger
      i/Logger
      {:enabled?
       (fn [^org.slf4j.Logger logger level]
         (condp = level
           :trace (.isTraceEnabled logger)
           :debug (.isDebugEnabled logger)
           :info  (.isInfoEnabled  logger)
           :warn  (.isWarnEnabled  logger)
           :error (.isErrorEnabled logger)
           :fatal (.isErrorEnabled logger)
           (throw (IllegalArgumentException. (str level)))))
       :write!
       (fn [^org.slf4j.Logger logger level ^Throwable e msg]
         (let [^String msg (str msg)]
           (if e
             (condp = level
               :trace (.trace logger msg e)
               :debug (.debug logger msg e)
               :info  (.info  logger msg e)
               :warn  (.warn  logger msg e)
               :error (.error logger msg e)
               :fatal (.error logger msg e)
               (throw (IllegalArgumentException. (str level))))
             (condp = level
               :trace (.trace logger msg)
               :debug (.debug logger msg)
               :info  (.info  logger msg)
               :warn  (.warn  logger msg)
               :error (.error logger msg)
               :fatal (.error logger msg)
               (throw (IllegalArgumentException. (str level)))))))})
    (reify i/LoggerFactory
      (name [_]
        "org.slf4j")
      (get-logger [_ logger-ns]
        (.getLogger factory ^String (str logger-ns))))))
