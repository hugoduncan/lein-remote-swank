# lein-remote-swank

This Leiningen plugin lets you launch a swank server on a remote machine via
Leiningen. Simply add [lein-remote-swank/lein-remote-swank "0.0.1"] and
[leiningen/lein-swank "1.1.0"] to your :dev-dependencies in project.clj and run
`lein remote-swank host` to start the server. Then from Emacs run M-x
slime-connect to connect to your project.

The plugin assumes you have a ssh key installed on the remote host.  The private
key path can be specified in the project file with the :private-key keyword.

The remote swank will be bound to localhost.  It can be accessed using ssh
tunnelling, for example `ssh -f -L 4006:localhost:4005 host`

You can then connect in emacs with M-x slime-connect on port 4006.

By default, copying is done with rsync, but this can be changed to scp of the
uberjar by specifying a non-nil value for :remote-uberjar. In this case
lein-swank is not required as a dev-dependency.

The rsync transfer respects any .rsync-filter file in the directory tree,
which can be used to filter which files are transfered.

Recognized keys in project.clj
-  :remote-user - remote user
-  :private-key - private key file path to use
-  :remote-path - remote path for the project
-  :remote-uberjar - if true, scp's the uberjar

There is a 600sec timeout on the rsync transfer, which might require you to run
it twice initially, depending on your connection speed.

You will need slime and slime-repl (but not swank-clojure) installed
[from ELPA](http://tromey.com/elpa) for this to work.

