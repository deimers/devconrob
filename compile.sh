sh ./clean.sh
mkdir bin
javac -d bin -sourcepath src -classpath .:lib/dio.jar src/*.java
