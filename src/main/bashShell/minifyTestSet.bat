@echo off
set %path%= "F:/Study/Research/RecoverJsName/RecoverJSName/resources/parsedData/testSet/"
cd %path%
set j=0
echo %j%
for /F "tokens=1" %%i in (fileList.txt) do (
	set "var=%path%%j%"
	set /A j=j+1
	echo %var%
	Rem this uglifyjs -m --keep-fnames %%i --output $var
)
