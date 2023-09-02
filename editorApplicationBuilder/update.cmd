@echo off
copy /y C:\Users\theto\IdeaProjects\Hasle\consoleJar\Hasle.jar C:\Users\theto\Desktop\Hasle\config\Console.jar
copy /y C:\Users\theto\IdeaProjects\Hasle\editorJar\Hasle.jar C:\Users\theto\Desktop\Hasle\Hasle.jar
cls
cd C:\Users\theto\Desktop\Hasle\
java -jar Hasle.jar
exit