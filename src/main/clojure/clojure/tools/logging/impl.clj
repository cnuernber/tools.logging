;; Copyright (c) Alex Taggart. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ^{:author "Alex Taggart"
      :doc "Protocols used to allow access to logging implementations.
            This namespace only need be used by those providing logging
            implementations to be consumed by the core api."}
     clojure.tools.logging.impl
  (:refer-clojure :exclude [name]))

(defprotocol Logger
  "The protocol through which the core api will interact with an underlying logging
  implementation.  Implementations should at least support the six standard
  logging levels if they wish to work from the level-specific macros."
  (enabled? [logger level]
    "Check if a particular level is enabled for the given Logger.")
  (write! [logger level throwable message]
    "Writes a log message to the given Logger."))

(defprotocol LoggerFactory
  "The protocol through which the core api will obtain an instance satisfying Logger
  as well as providing information about the particular implementation being used.
  Implementations should be bound to *logger-factory* in order to be picked up by
  this library."
  (name [factory]
    "Returns some text identifying the underlying implementation.")
  (get-logger [factory logger-ns]
    "Returns an implementation-specific Logger by namespace."))

(def disabled-logger
  "A Logger that is not enabled and does nothing on write."
  (reify Logger
    (enabled? [_ _] false)
    (write! [_ _ _ _])))

(def disabled-logger-factory
  "A LoggerFactory that always provides the disabled-logger."
  (reify LoggerFactory
    (name [_] "disabled")
    (get-logger [_ _] disabled-logger)))

(defn class-found?
  "Returns true if the Class associated with the given classname can be found
   using the context ClassLoader for the current thread."
  [name]
  (try
    (Class/forName name true (.. Thread currentThread getContextClassLoader))
    true
    (catch ClassNotFoundException _
      false)))


(defn slf4j-factory
  "Returns a SLF4J-based implementation of the LoggerFactory protocol, or nil if
  not available."
  []
  (when (class-found? "org.slf4j.Logger")
    ((requiring-resolve 'clojure.tools.logging.slf4j/init))))


(defn cl-factory
  "Returns a Commons Logging-based implementation of the LoggerFactory protocol, or
  nil if not available."
  []
  (when (class-found? "org.apache.commons.logging.Log")
    ((requiring-resolve 'clojure.tools.logging.cl/init))))

(defn log4j-factory
  "Returns a Log4j-based implementation of the LoggerFactory protocol, or nil if
  not available."
  []
  (when (class-found? "org.apache.log4j.Logger")
    ((requiring-resolve 'clojure.tools.logging.log4j/init))))

(defn log4j2-factory
  "Returns a Log4j2-based implementation of the LoggerFactory protocol, or nil if
  not available."
  []
  (when (class-found? "org.apache.logging.log4j.Logger")
    ((requiring-resolve 'clojure.tools.logging.log4j2/init))))

(defn jul-factory
  "Returns a java.util.logging-based implementation of the LoggerFactory protocol,
  or nil if not available."
  []
  (when (class-found? "java.util.logging.Logger")
    ((requiring-resolve 'clojure.tools.logging.jul/init))))

(defn find-factory
  "Returns the first non-nil value from slf4j-factory, cl-factory,
   log4j2-factory, log4j-factory, and jul-factory."
  []
  (or (slf4j-factory)
      (cl-factory)
      (log4j2-factory)
      (log4j-factory)
      (jul-factory)
      (throw ; this should never happen in 1.5+
        (RuntimeException.
          "Valid logging implementation could not be found."))))
