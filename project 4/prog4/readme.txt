InvertedIndexingLocal.java
Synopsis: Read all files from a given directory into IMap, find which cluster node contains which <key, value> items, and locally print out all file names that contain a given keyword.
Textbook: p11 and p71
Compile:  ./compileAll.sh
Usage:   
node1: ./console.sh
node2: ./console.sh
node3: ./console.sh
node4: ./run.sh DBCreator ~path/prog3/rfc
node5: ./run.sh LocalKeyViewer
nodde6: ./run.sh InvertedIndexingLocal TCP

InvertedIndexingRmote.java and InvertedIndexingEach.java
Synopsis: Read all files from a given directory into IMap, let a callable object run at each remote machine, examin <key, value> items local to that remote machine, and return all file names that contain a given keyword.
Textbook: p11 and p71
Compile:  ./compileAll.sh
Usage:   
node1: ./console.sh
node2: ./console.sh
node3: ./console.sh
node4: ./run.sh DBCreator ~path/prog3/rfc
node5: ./run.sh LocalKeyViewer
node6: ./run.sh InvertedIndexingLocal TCP



