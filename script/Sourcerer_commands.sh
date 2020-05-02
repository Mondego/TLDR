#!/bin/bash



cd /scratch/mondego/local/Maruf/Dependencies/Experiment/Sourcerer/bin/dist/

java -jar java-repo-tools.jar --aggregate-jar-files --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/crawled-projects


cd /scratch/mondego/local/Maruf/Dependencies/Experiment/eclipse1/plugins/

java -jar  org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar  -consolelog -application Extractor.Extractor

java -jar org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar -application Extractor.Extractor --add-libraries-to-repo --output-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/crawled-projects

java -jar org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar -application Extractor.Extractor --extract-libraries --output-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/crawled-projects

java -jar org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar -application Extractor.Extractor  --extract-maven-jars --output-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/crawled-projects

java -jar org.eclipse.equinox.launcher_1.3.100.v20150511-1540.jar -application Extractor.Extractor  --extract-projects --output-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/crawled-projects

cd /scratch/mondego/local/Maruf/Dependencies/Experiment/Sourcerer/bin/dist/

java -jar db-import.jar --database-url jdbc:mysql://localhost/ASE --database-user maruf --database-password maruf --initialize-db


java -jar db-import.jar --database-url jdbc:mysql://localhost/ASE --database-user maruf --database-password maruf --add-libraries --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --output /scratch/mondego/local/Maruf/Dependencies/Experiment/db-import-output/


java -jar db-import.jar --database-url jdbc:mysql://localhost/ASE --database-user maruf --database-password maruf --add-jars --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --output /scratch/mondego/local/Maruf/Dependencies/Experiment/db-import-output/

java -jar db-import.jar --database-url jdbc:mysql://localhost/ASE --database-user maruf --database-password maruf --add-projects --input-repo /scratch/mondego/local/Maruf/Dependencies/Experiment/extracted-projects --output /scratch/mondego/local/Maruf/Dependencies/Experiment/db-import-output/

