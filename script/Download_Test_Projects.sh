#!/bin/bash


PROJECT_COLLECTION_DIRECTORY="projects"

mkdir -p $PROJECT_COLLECTION_DIRECTORY
cd $PROJECT_COLLECTION_DIRECTORY

git clone https://github.com/headius/invokebinder.git &
git clone https://github.com/google/compile-testing.git &
git clone https://github.com/apache/commons-cli.git &
git clone https://github.com/logstash/logstash-logback-encoder.git &
git clone https://github.com/apache/commons-dbutils.git &
git clone https://github.com/apache/commons-validator.git &
git clone https://github.com/apache/commons-fileupload.git &
git clone https://github.com/apache/commons-codec.git &
git clone https://github.com/asterisk-java/asterisk-java.git &
git clone https://github.com/ninjaframework/ninja.git &
git clone https://github.com/robovm/robovm.git &
git clone https://github.com/OryxProject/oryx.git &
git clone https://github.com/graphhopper/graphhopper.git &
git clone https://github.com/AdoptOpenJDK/jitwatch.git &
git clone https://github.com/apache/commons-collections.git &
git clone https://github.com/apache/commons-lang.git &
git clone https://github.com/square/retrofit.git &
git clone https://github.com/apache/commons-email.git &
git clone https://github.com/apache/commons-compress.git &
git clone https://github.com/apache/commons-imaging.git &
git clone https://github.com/apache/commons-functor.git &
git clone https://github.com/apache/commons-jxpath.git &
git clone https://github.com/apache/bval.git &
git clone https://github.com/JodaOrg/joda-time.git &
git clone https://github.com/cucumber/cucumber.git &
git clone https://github.com/apache/commons-math.git &
git clone https://github.com/apache/commons-io.git &
git clone https://github.com/google/closure-compiler.git &
git clone https://github.com/apache/commons-net.git &
git clone https://github.com/opentripplanner/OpenTripPlanner.git &
git clone https://github.com/apache/commons-pool.git &
git clone https://github.com/OpenHFT/Chronicle-Map.git
