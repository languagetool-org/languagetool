set args = WScript.Arguments
Set fso = CreateObject("Scripting.FileSystemObject")
WScript.Echo fso.GetFileVersion(args(0))
Wscript.Quit