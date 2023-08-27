@echo off

:: %1 = path for application (includes shortcut)

set consoleJar=C:\Users\theto\IdeaProjects\Hasle\consoleJar\Hasle.jar
set editorJar=C:\Users\theto\IdeaProjects\Hasle\editorJar\Hasle.jar
set builderRoot=C:\Users\theto\IdeaProjects\Hasle\editorApplicationBuilder\
set settings=%builderRoot%settings-template.txt
set icon=%builderRoot%hasle.ico
set png=%builderRoot%hasle.png
set runBat=%builderRoot%run.bat

:: sets up dirs
md %1\Hasle
cd %1\Hasle
md config
md programs

:: copies JARs
copy %consoleJar% config
ren config\Hasle.jar Console.jar
copy %editorJar%

:: copies config files
cd config
copy %settings%
ren settings-template.txt settings.txt
copy %icon%
copy %png%
copy %runBat%

:: creates autosave.txt
cd..
cd programs
cd. > autosave.txt

:: creates shortcut
call:CreateShortcut %1\Hasle\Hasle.jar "Hasle" %icon% %1 %1\Hasle

exit

::https://stackoverflow.com/a/36858017
::****************************************************************************************************
:CreateShortcut <ApplicationPath> <ShortcutName> <Icon> <Location> <StartIn>
(
echo Call Shortcut("%~1","%~2","%~3","%~4","%~5"^)
echo ^'**********************************************************************************************^)
echo Sub Shortcut(ApplicationPath,Name,Icon,Location,StartIn^)
echo    Dim objShell,DesktopPath,objShortCut,MyTab
echo    Set objShell = CreateObject("WScript.Shell"^)
echo    MyTab = Split^(ApplicationPath,"\"^)
echo    If Name = "" Then
echo    Name = MyTab(UBound^(MyTab^)^)
echo    End if
echo    Set objShortCut = objShell.CreateShortcut(Location ^& "\" ^& Name ^& ".lnk"^)
echo    objShortCut.TargetPath = Dblquote^(ApplicationPath^)
echo    ObjShortCut.IconLocation = Icon
echo    ObjShortCut.WorkingDirectory = StartIn
echo    objShortCut.Save
echo End Sub
echo ^'**********************************************************************************************
echo ^'Fonction pour ajouter les doubles quotes dans une variable
echo Function DblQuote(Str^)
echo    DblQuote = Chr(34^) ^& Str ^& Chr^(34^)
echo End Function
echo ^'**********************************************************************************************
)> Shortcutme.vbs
Start /wait Shortcutme.vbs
Del Shortcutme.vbs
Exit /b
::****************************************************************************************************
