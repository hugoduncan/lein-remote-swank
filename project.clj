(defproject lein-remote-swank/lein-remote-swank "0.0.1-SNAPSHOT"
  :description "A leiningen plugin to start a remote swank server."
  :dependencies [[swank-clojure "1.1.0"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]
                 [org.clojure/clojure-contrib "1.1.0-master-SNAPSHOT"]
		 [org.apache.commons/commons-exec "1.0.1"]
		 [crane "1.0-SNAPSHOT"
		  :exclusions
		  [net.java.dev.jets3t/jets3t
		   com.google.code.typica/typica
		   org.jclouds/jclouds-blobstore
		   org.jclouds/jclouds-compute
		   org.jclouds/jclouds-azure
		   org.jclouds/jclouds-atmos
		   org.jclouds/jclouds-aws
		   org.jclouds/jclouds-rackspace
		   org.jclouds/jclouds-terremark
		   org.jclouds/jclouds-hostingdotcom
		   org.jclouds/jclouds-rimuhosting
		   org.jclouds/jclouds-jsch
		   org.jclouds/jclouds-log4j
		   org.jclouds/jclouds-enterprise
		   log4j/log4j
		   jline]]])

