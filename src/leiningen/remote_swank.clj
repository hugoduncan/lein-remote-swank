(ns leiningen.remote-swank
  (:use [leiningen.uberjar :only [uberjar]]
	[leiningen.compile :only [eval-in-project]]
	[clojure.contrib.java-utils :only [file]]
	[clojure.contrib.pprint :only [pprint]]
	[crane.ssh2 :only [session put with-connection shell-channel sh!]]))

(defn default-private-key-path []
  (str (. System getProperty "user.home") "/.ssh/id_rsa"))

(defn default-user []
  (. System getProperty "user.name"))

(defn command-line
  [path options]
  (str "java " options " -cp " path " swank.swank"))

(defn remote-swank
  "Launch swank server on remote machine for Emacs to connect."
  ([project & args]
     (uberjar project)
     (let [[server port & other-args] args
	   user (or (:remote-user project) (default-user))
	   remote-path (str "/home/" user)
	   jar-file (str (:name project) "-standalone.jar")]

       (with-connection [connection (session (or (:private-key project) (default-private-key-path))
					     user
					     server)]
	 (put connection
	      (file (:root project) jar-file)
	      (str "/home/" user))
	 (let [channel (shell-channel connection)]
	   (pprint
	    (seq (.split #"\r?\n"
			 (sh! channel (command-line (str (file remote-path jar-file)) (:java-opts project)))))))))))

