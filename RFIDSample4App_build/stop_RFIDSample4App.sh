# Stop script for sampleapp
echo "Stopping application"
EXECUTABLE_NAME=RFIDSample4App.jar
PID=`ps -C "java -jar ${EXECUTABLE_NAME}" -o pid=`
kill -9 $PID
unset EXECUTABLE_NAME
unset PID
#end of Stop script