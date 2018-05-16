echo ===========================================================================
echo --- Create executable Jar file IBMiSqlUpdate.jar
echo ===========================================================================

echo The following command gets the directory in which the script is placed

script_dir=$(dirname $0)
echo script_dir=$(dirname $0)
echo $script_dir

echo -------------------------------------------------------------
echo The following command makes the application directory current

cd $script_dir
echo cd $script_dir

echo -------------------------------------------------------------------
echo The following command creates the Jar file in the current directory

echo jar cvfm  IBMiSqlUpdate.jar  manifesIBMiSqlUpdate.txt  -C build/classes  update/U_MainWindow_Parameters.class  -C build/classes  update -C build/classes locales
jar cvfm  IBMiSqlUpdate.jar  manifestIBMiSqlUpdate.txt  -C build/classes  update/U_MainWindow_Parameters.class  -C build/classes  update -C build/classes locales

echo -------------------------------------------
echo The following command executes the Jar file

echo java -jar IBMiSqlUpdate.jar
java -jar IBMiSqlUpdate.jar