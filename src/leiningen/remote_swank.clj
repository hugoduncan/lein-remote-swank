(ns leiningen.remote-swank
  (:use [leiningen.uberjar :only [uberjar]]
        [leiningen.compile :only [eval-in-project]]
        [clojure.contrib.java-utils :only [file]]
        [clojure.contrib.pprint :only [pprint]]
        [crane.ssh2 :only [session put with-connection shell-channel sh!]])
  (:import (org.apache.commons.exec CommandLine
                                    DefaultExecutor
                                    ExecuteWatchdog)))

(defn default-private-key-path []
  (str (. System getProperty "user.home") "/.ssh/id_rsa"))

(defn default-user []
  (. System getProperty "user.name"))

(defn java-command-line
  [options path port]
  (str "java " options " -cp " path " swank.swank " port))

(defn remote-copy-jar [project user private-key server port remote-path]
  (uberjar project)
  (let [jar-file (str (:name project) "-standalone.jar")]
    (println "Copying jar ...")
    (with-connection [connection (session private-key user server)]
      (put connection (file (:root project) jar-file) (str "/home/" user))
      (println "Starting remote swank ...")
      (let [channel (shell-channel connection)]
        (pprint (seq (.split #"\r?\n"
           (sh! channel (java-command-line
                         (str (file remote-path jar-file))
                         (:java-opts project) port)))))))))

(defn system
  "Launch a system process, return a string containing the process output."
  [cmd]
  (let [command-line (CommandLine/parse cmd)
        executor (DefaultExecutor.)
        watchdog  (ExecuteWatchdog. 600000)]
    (.setExitValue executor 0)
    (.setWatchdog executor watchdog)
    (.execute executor command-line)))

(defn remote-rsync [project user private-key server port remote-path]
  (println "Rsyncing project ...")
  (system (str "/usr/bin/rsync -rP --delete -F -F " (:root project)  " "
               user "@" server ":" remote-path))
  (with-connection [connection (session private-key user server)]
    (println "Starting remote lein swank ...")
    (let [channel (shell-channel connection)]
      (pprint (seq (.split #"\r?\n"
       (sh! channel (str "cd " remote-path "/"
        (:name project) " && lein swank"))))))))

(defn remote-swank
  "Launch swank server on remote machine for Emacs to connect."
  ([project & args]
     (let [[server port & other-args] args
           user (or (:remote-user project) (default-user))
           private-key (or (:private-key project) (default-private-key-path))
           remote-path (or (:remote-path project) (str "/home/" user))]
       (if (:remote-uberjar project)
         (remote-copy-jar project user private-key server port remote-path)
         (remote-rsync project user private-key server port remote-path)))))

