
# Blazegraph install
====================================  
Doc: https://www.blazegraph.com/

1.- Download blazegraph from https://www.blazegraph.com/  
2.- Copy .jar in server folder (eg. /servers/lod/blazegraph)  
3.- Start BlazeGraph in eclipse. See "lod.eclipse.read.me" file.  
4.- http://localhost:9999/blazegraph in browser.  

# BlazeGraph in eclipse
===========================

1.- Goto `[Run] > [External Tools] > [External Tools Configuration...]`

2.- Create a NEW config with:

    |--------------------|---------------------|-----------------------------------------------------------|
    | Location:          | jdk location        | eg. "/develop/java/jdk1.8.0_121/bin/java"                 |
    | Working Directory: | blazegraph location | eg. "/servers/lod/blazegraph"                             |
    | Arguments:         | blazegraph jar      | eg. "-server -jar /servers/lod/blazegraph/blazegraph.jar" |
