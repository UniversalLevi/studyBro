@echo off
mkdir -p app\src\main\java\com\example\studymate\data\source\local
mkdir -p app\src\main\java\com\example\studymate\data\repository
mkdir -p app\src\main\java\com\example\studymate\service
mkdir -p app\src\main\java\com\example\studymate\receiver
mkdir -p app\src\main\java\com\example\studymate\ui\navigation
mkdir -p app\src\main\java\com\example\studymate\ui\screens\home
mkdir -p app\src\main\java\com\example\studymate\ui\screens\tasks
mkdir -p app\src\main\java\com\example\studymate\ui\screens\timer
mkdir -p app\src\main\java\com\example\studymate\ui\screens\stats
mkdir -p app\src\main\java\com\example\studymate\ui\screens\settings
mkdir -p app\src\main\java\com\example\studymate\ui\theme
mkdir -p app\src\main\java\com\example\studymate\util

rem Copy data model files
copy app\src\main\java\com\example\myapplication\data\model\*.* app\src\main\java\com\example\studymate\data\model\

rem Copy data source files
copy app\src\main\java\com\example\myapplication\data\source\local\*.* app\src\main\java\com\example\studymate\data\source\local\

rem Copy repository files
copy app\src\main\java\com\example\myapplication\data\repository\*.* app\src\main\java\com\example\studymate\data\repository\

rem Copy service files
copy app\src\main\java\com\example\myapplication\service\*.* app\src\main\java\com\example\studymate\service\

rem Copy receiver files
copy app\src\main\java\com\example\myapplication\receiver\*.* app\src\main\java\com\example\studymate\receiver\

rem Copy UI files
copy app\src\main\java\com\example\myapplication\ui\navigation\*.* app\src\main\java\com\example\studymate\ui\navigation\
copy app\src\main\java\com\example\myapplication\ui\screens\home\*.* app\src\main\java\com\example\studymate\ui\screens\home\
copy app\src\main\java\com\example\myapplication\ui\screens\tasks\*.* app\src\main\java\com\example\studymate\ui\screens\tasks\
copy app\src\main\java\com\example\myapplication\ui\screens\timer\*.* app\src\main\java\com\example\studymate\ui\screens\timer\
copy app\src\main\java\com\example\myapplication\ui\screens\stats\*.* app\src\main\java\com\example\studymate\ui\screens\stats\
copy app\src\main\java\com\example\myapplication\ui\screens\settings\*.* app\src\main\java\com\example\studymate\ui\screens\settings\
copy app\src\main\java\com\example\myapplication\ui\theme\*.* app\src\main\java\com\example\studymate\ui\theme\

rem Copy util files
copy app\src\main\java\com\example\myapplication\util\*.* app\src\main\java\com\example\studymate\util\

rem Copy main files
copy app\src\main\java\com\example\myapplication\*.* app\src\main\java\com\example\studymate\

echo Files copied successfully! 