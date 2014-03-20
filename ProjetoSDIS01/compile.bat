mkdir P1
mkdir P2
mkdir P3
mkdir P4
mkdir P5
mkdir P6
echo. 2>run.bat
echo call java Main>run.bat
copy run.bat P1\
copy run.bat P2\
copy run.bat P3\
copy run.bat P4\
copy run.bat P5\
copy run.bat P6\
copy "C:\Users\Papa Formigas\IdeaProjects\MIEIC\SDIS_Proj\SDIS_Projeto\src\*.java"
call javac *.java
call copy *.class P1\ /Y
call copy *.class P2\ /Y
call copy *.class P3\ /Y
call copy *.class P4\ /Y
call copy *.class P5\ /Y
call copy *.class P6\ /Y
del *.class
start /D P1 java Main
start /D P2 java Main
start /D P3 java Main
start /D P4 java Main
start /D P5 java Main
start /D P6 java Main