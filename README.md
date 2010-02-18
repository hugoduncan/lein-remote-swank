# lein-remote-swank

This Leiningen plugin lets you launch a swank server on a remote machine via
Leiningen. Simply add [leiningen/lein-remote-swank "0.0.1"] to your
:dev-dependencies in project.clj and run "lein remote-swank host" to start the
server. Then from Emacs run M-x slime-connect to connect to your project.

The plugin assumes you have a ssh key installed on the remote host.  The private
key path can be specified in the project file with the :private-key keyword.

The remote swank will be bound to localhost.  It can be accessed using ssh
tunnelling, for example `ssh -f -L ssh -L 4006:localhost:4005 host`

You can then connect in emacs with M-x slime-connect.

You will need slime and slime-repl (but not swank-clojure) installed
[from ELPA](http://tromey.com/elpa) for this to work.

