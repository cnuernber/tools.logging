(ns clojure.tools.logging.cl
  (:require [clojure.tools.logging.impl :as i]))

(defn init []
  (let [                         ; Same as is done inside LogFactory/getLog(String).
            factory (org.apache.commons.logging.LogFactory/getFactory)]
        (extend org.apache.commons.logging.Log
          i/Logger
          {:enabled?
           (fn [^org.apache.commons.logging.Log logger level]
             (condp = level
               :trace (.isTraceEnabled logger)
               :debug (.isDebugEnabled logger)
               :info  (.isInfoEnabled  logger)
               :warn  (.isWarnEnabled  logger)
               :error (.isErrorEnabled logger)
               :fatal (.isFatalEnabled logger)
               (throw (IllegalArgumentException. (str level)))))
           :write!
           (fn [^org.apache.commons.logging.Log logger level e msg]
             (if e
               (condp = level
                 :trace (.trace logger msg e)
                 :debug (.debug logger msg e)
                 :info  (.info  logger msg e)
                 :warn  (.warn  logger msg e)
                 :error (.error logger msg e)
                 :fatal (.fatal logger msg e)
                 (throw (IllegalArgumentException. (str level))))
               (condp = level
                 :trace (.trace logger msg)
                 :debug (.debug logger msg)
                 :info  (.info  logger msg)
                 :warn  (.warn  logger msg)
                 :error (.error logger msg)
                 :fatal (.fatal logger msg)
                 (throw (IllegalArgumentException. (str level))))))})
        (reify i/LoggerFactory
          (name [_]
            "org.apache.commons.logging")
          (get-logger [_ logger-ns]
            (.getInstance factory (str logger-ns))))))
