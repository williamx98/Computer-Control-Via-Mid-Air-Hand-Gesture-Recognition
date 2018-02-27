cd..
set mypath=%cd%
cd %mypath%/src
javac -cp "%mypath%\rsc\*" -d "%mypath%\bin" test.java
cd %mypath%/bin
java -cp ".;%mypath%\rsc\*" -Xms512m test
