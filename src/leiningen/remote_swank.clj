(ns leiningen.remote-swank
  "Run a lein project on a remote machine."
  (:require
   clojure.contrib.pprint
   [clj-ssh.ssh :as ssh]))

(try
  (require '[clojure.contrib.io :as io])
  (use '[clojure.contrib.io :only [file]])
  (require '[clojure.contrib.shell :as shell])
  (catch Exception e
    (require '[clojure.contrib.duck-streams :as io])
    (use '[clojure.contrib.java-utils :only [file]])
    (require '[clojure.contrib.shell-out :as shell])))

(defn default-private-key-path []
  (str (. System getProperty "user.home") "/.ssh/id_rsa"))

(defn default-user []
  (. System getProperty "user.name"))

(defn java-command-line
  [options path port]
  (str "java " options " -cp " path " swank.swank " port))

(defn sh-script
  "Run a script on local machine."
  [command]
  (let [tmp (java.io.File/createTempFile "remoteswank" "script")]
    (try
     (io/copy command tmp)
     (shell/sh "chmod" "+x" (.getPath tmp))
     (let [result (shell/sh "bash" (.getPath tmp) :return-map true)]
       (when (pos? (result :exit))
         (println (str "Command failed: " command "\n" (result :err))))
       result)
     (finally  (.delete tmp)))))

(defn remote-rsync [project user private-key server port remote-path]
  (println "Rsyncing project ...")

  (let [rv (sh-script
            (format
             "/usr/bin/rsync -e '/usr/bin/ssh -o \"StrictHostKeyChecking %s\" -i \"%s\"' -rP --delete -F -F %s %s@%s:%s"
             (if (:strict-host-key-checking project false) "yes" "no")
             private-key
             (:root project) user server remote-path))]
    (if (pos? (:exit rv))
      (do
        (println "Problem executing rsync:")
        (println (:err rv))
        (println (:out rv)))
      (ssh/with-ssh-agent []
        (println (:out rv))
        (ssh/add-identity-with-keychain private-key)
        (let [session (ssh/session
                       ssh/*ssh-agent*
                       server
                       :strict-host-key-checking
                       (if (:strict-host-key-checking project false) "yes" "no"))]
          (ssh/with-connection session
            (println "Starting remote lein swank ...")
            (let [rv (ssh/ssh
                      session
                      (format "cd %s/%s && lein swank" remote-path (:name project))
                      :return-map true)
                  out (:out rv)
                  err (:err rv)
                  exit (:exit rv)]
              (if (pos? exit)
                (println "ERROR: " err))
              (clojure.contrib.pprint/pprint (seq (.split #"\r?\n" out))))))))))

(defn remote-swank
  "Launch swank server on remote machine for Emacs to connect.
   Specify the server and port as arguments."
  ([project & args]
     (let [[server port] args
           user (:remote-user project (default-user))
           private-key (:private-key project (default-private-key-path))
           remote-path (:remote-path project (str "~" user))]
       (remote-rsync project user private-key server (or port 4005) remote-path))))

