import java.io.IOException;
import java.io.*;
import java.util.*;
import java.nio.file.*;

/*
   Author : Dewey Milton
   Date : 10/15/17
   Class : 541
   Algebra
   Contains methods representing relational algebra calculations
 */
public class Algebra {


        private File file;
        private String content;
        private File file2;
        private File newFile;
        private File logFile;
        private int rowCount;
        private String resColumn;
        private String op;
        private String value;
        private int resDex1;
        private int resDex2;
        private boolean errorCheck = false;

        private void createHeader(ArrayList<String> columns, String _newFile){

            int count = 0;

            String header = "";

            try {
                PrintWriter writer = new PrintWriter(_newFile, "UTF-8");

                while(count < columns.size()){

                    header += columns.get(count);

                    count++;

                    if(count < columns.size()){
                        header += "~";
                    }
                }

                writer.println(header);
                writer.close();
                this.newFile = new File(_newFile);

            }catch(IOException ex){
                System.out.println("Unable to create file");
            }
        }

        /*
          Project : String String String -> void
          Given a file name, and a string of column names, the method creates a new file of a given name
          At most 20 columns
          First row of the given file is of column names
          All column names and values are limited to 16 characters
         */

         public void Project(String oldFile, String select, String _newFile){

             File org = new File(oldFile);
             this.file = org;
             File file = new File(_newFile);
             this.newFile = file;

             if(!_newFile.equals("")){
                 writeLog(_newFile);
             }

             //grabs all the column titles from original file
             ArrayList<String> columns = getColumns(org);

//             for(String column: columns){
//                 System.out.println(column);
//             }

             //A list of all selected column titles wanted back
             ArrayList<String> newColumns = getSelected(select);

             //Checks to see if columns exist
             boolean check = check(columns, newColumns);

             //Creates a new file header and then associated rows for a new table
             if(check && newColumns.size() > 0) {

                 createHeader(newColumns, _newFile);

                 makeTable(columns, newColumns);

                 System.out.println("Success.");

             }else{
                 System.out.println("Unable to project this table.");
             }

         }


        /*
           Joins two files together into one new larger file
           String fileName, String fileName, String newFileName -> void
         */
        public void Join(String _file, String _file2, String newFile){

            File file = new File(_file);
            File file2 = new File(_file2);

            if(!newFile.equals("")){
                writeLog(newFile);
            }

            ArrayList<String> header1 = getColumns(file);
            ArrayList<String> header2 = getColumns(file2);

            //Combines only if there is two files
            if(header1.size() > 0 && header2.size() > 0){

                //Removes all duplicate header titles
                ArrayList<String> newHeader2 = removeDupl(header1, header2);

                //Creates a new list of combined column names
               ArrayList<String> newHeader = joinHeaders(header1, newHeader2);

                createHeader(newHeader, newFile);

                joinTables(file, file2, header1, header2, newHeader2, newFile);

                System.out.println("Success.");

            }else{
                System.out.println("Unable to join tables.");
            }

        }


        /*
           Restrict takes a file name and a restriction string which is used to create a new file matching restriction string criteria
           example of a restriction string : Price>='19000'
         */
        public void Restrict(String _file, String res, String _newFile){

                File file = new File(_file);

            if(!_newFile.equals("")){
                writeLog(_newFile);
            }

            if(file == null){
                this.errorCheck = true;
            }else {

                ArrayList<String> header = getColumns(file);

                //resets all values to blank
                this.resColumn = "";
                this.value = "";
                this.op = "";
                this.resDex1 = 0;
                this.resDex2 = 0;
                this.newFile = new File(_newFile);
                //checks if comparing another column or a value, while parsing through and instantiating header name, operator, and value.
                boolean colRes = getWhere(res);

                if (colRes) {
                    //Check if compared to column exist
                    boolean colCheck = colCheck(this.value, header);

                    if (!colCheck) {
                        this.errorCheck = true;
                    }
                }

                //finds the index position of column being restricted.
                this.resDex1 = getColPos(header, this.resColumn);

                if (!this.errorCheck) {

                    //find position of second column being compared too
                    if (colRes) {
                        this.resDex2 = getColPos(header, this.value.toUpperCase());
                    }

                    //re-creates a new file containing only the restricted content
                    try {
                        Scanner scan = new Scanner(file);

                        try (FileWriter fw = new FileWriter(this.newFile);
                             BufferedWriter bw = new BufferedWriter(fw);
                             PrintWriter out = new PrintWriter(bw)) {
                            //Create header of new file
                            out.println(scan.nextLine());

                            while (scan.hasNextLine()) {

                                String line = scan.nextLine();

                                //if comparing to another column
                                if (colRes) {

                                    String value1 = getValue(line, this.resDex1);

                                    String value2 = getValue(line, this.resDex2);

                                    if (compareVal(value1, value2, this.op)) {

                                        out.println(line);
                                    }

                                } else {
                                    String value1 = getValue(line, this.resDex1);

                                    if (compareVal(value1, this.value, this.op)) {
                                        out.println(line);
                                    }
                                }

                            }
                            scan.close();
                            out.close();
                            bw.close();

                            System.out.println("Success");

                        } catch (IOException e) {
                            //exception handling left as an exercise for the reader
                        }

                    } catch (IOException ex) {
                        System.out.println("Unable to create table.");
                    }

                    //  System.out.println("Position in header " + this.resDex1 + " Error check is " + this.errorCheck);
                } else {
                    System.out.println("Error finding restriction column");
                }

            }
            if(this.errorCheck){
                System.out.println("File not Found");
            }
        }

        //helper method that takes a line from a file and the position of a chosen to column
        //using ~ delimiter, this will parse the value from the line and return the value as a string
        private String getValue(String line, int valPos){
            String value = "";

          //  System.out.println("Line is :" + line);

            Scanner scan = new Scanner(line);
            scan.useDelimiter("~");

            int pos = 0;

            boolean done = false;

            while(!done && pos <= valPos){

                if(pos == valPos){
                    value = scan.next();
                    done = true;
                }else{
                    scan.next();
                }
                pos++;
            }

            scan.close();
           // System.out.println("Found value : " + value);

            return value;
        }

        //String value1, String value2, String operator -> boolean
        // used to check for string or digit comparison and return result
        private boolean compareVal(String val1, String val2, String op){

            boolean compare = false;
            boolean isNum = false;
            int num1 = 0;
            int num2 = 0;

            //checks if value2 is a digit
            if(val2.charAt(0) >= 48 && val2.charAt(0) <= 57) {
                isNum = true;

                int calc = 1;
                //builds an integer number starting in the ones position
                for (int pos = val1.length() - 1; pos >= 0; pos--) {
                    num1 += val1.charAt(pos) * calc;
                    calc = calc * 10;
                }
                calc = 1;
                for (int pos = val2.length() - 1; pos >= 0; pos--) {
                    num2 += val2.charAt(pos) * calc;
                    calc = calc * 10;
                }
            }else{
                //if found to be String, sets values to lower case for easy comparison
                val1 = val1.toLowerCase();
                val2 = val2.toLowerCase();
            }

            switch(op){
                case "=":
                    if(isNum){
                        if(num1 == num2){
                            compare = true;
                        }
                    }else {
                        if (val1.equalsIgnoreCase(val2)) {
                            compare = true;
                        }
                    }
                    break;
                case "!=":
                    if(isNum){
                        if(num1 != num2){
                            compare = true;
                        }
                    }else {
                        if (!val1.equalsIgnoreCase(val2)) {
                            compare = true;
                        }
                    }
                    break;
                case ">":
                    if(isNum){
                        if(num1 > num2){
                            compare = true;
                        }
                    }else {
                        if(val1.charAt(0) > val2.charAt(0)){
                            compare = true;
                        }
                    }
                    break;
                case ">=":
                    if(isNum){
                        if(num1 >= num2){
                            compare = true;
                        }
                    }else {
                        if (val1.charAt(0) >= val2.charAt(0)) {
                            compare = true;
                        }
                    }
                    break;
                case "<":
                    if(isNum){
                        if(num1 < num2){
                            compare = true;
                        }
                    }else {
                        if (val1.charAt(0) < val2.charAt(0)) {
                            compare = true;
                        }
                    }
                    break;
                case "<=":
                    if(isNum){
                        if(num1 <= num2){
                            compare = true;
                        }
                    }else {
                        if (val1.charAt(0) <= val2.charAt(0)) {
                            compare = true;
                        }
                    }
                    break;
            }

            return compare;
        }

        /*Given a string value and list of header names
          Returns true if value is found in the list
        */
        private boolean colCheck(String value, ArrayList<String> header){

            boolean check = false;

            for(String column : header){

                if(column.equals(value.toUpperCase())){
                    check = true;
                }
            }

            return check;
        }

        /*
           Given a list of header names and a target
           Returns the index position where the target is found
         */
        private int getColPos(ArrayList<String> columns, String target){
            int pos = 0;
            boolean found = false;

            while(!found && pos < columns.size()){
                if(columns.get(pos).equals(target)){
                  //  System.out.println("Found at pos " + pos + " At values " + columns.get(pos) + " Equals target " + target);
                    found = true;
                }else{
                    pos++;
                }
            }

            if(!found){
                this.errorCheck = true;
            }

            return pos;
        }


       //Helper function that parses through the restrict string pulling out header name, operator, and value
        private boolean getWhere(String res){

            boolean colRes = true;

            int pos = 0;
            boolean end = false;
            while(!end){

                //Grabs the restricted column name by parsing through restrictive string until reaching non-alpha character
                if(!(res.charAt(pos) >= 65 && res.charAt(pos) <= 90 || res.charAt(pos) >= 97 && res.charAt(pos) <= 122)){
                    end = true;

                }else {
                    this.resColumn += res.charAt(pos);
                    pos++;
                }

                if(pos >= res.length()){

                    end = true;
                }
            }

            //System.out.println(this.resColumn);

            this.resColumn = this.resColumn.toUpperCase();

            //grabs the logic operator
            if(pos < res.length()) {
                //This needs to parse until it reaches ' or char code 39
                while (res.charAt(pos) >= 60 && res.charAt(pos) <= 62 || res.charAt(pos) == 33 && pos < res.length()) {
                    this.op += "" + res.charAt(pos);
                    pos++;
                }
            }else{
                this.errorCheck = true;
            }

            //System.out.println(this.op);

            if(pos < res.length()) {
                //checks if current position contains ' character. If so, then static value is being checked, not column
                if (res.charAt(pos) == 39) {
                    colRes = false;
                    pos++;
                }
            }else{
                this.errorCheck = true;
            }

            int endLength;

            if(!colRes){
                endLength = res.length() - 1;
            }else{
                endLength = res.length();
            }

            //grabs the value being compared
            while(pos < endLength){
                this.value += res.charAt(pos);
                pos++;
            }

           // System.out.println(this.value);

            if(this.value.equals("") || this.op.equals("")){
                this.errorCheck = true;
            }

            return colRes;

        }

        //Helper function for join function
        //Takes two files, the header of each file, and the String of the new file name
        //Creates a new file joining the two files together one line at a time
        private void joinTables(File file, File file2, ArrayList<String> header1, ArrayList<String> header2, ArrayList<String> newHeader2, String newFile){

            if(!newFile.equals("")){
                writeLog(newFile);
            }

            try {
                Scanner scan1 = new Scanner(file);
                Scanner scan2 = new Scanner(file2);

                ArrayList<Integer> foundLines = new ArrayList<>();

                //this code will append to a file
                try(FileWriter fw = new FileWriter(newFile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw))
                {
                    //Skip headers
                    scan1.nextLine();
                    scan2.nextLine();

                    while(scan1.hasNextLine() || scan2.hasNextLine()){

                        String line1 = "";
                        String line2 = "";

                        if(scan1.hasNextLine()) {
                            line1 = scan1.nextLine();
                        }

                        if(scan2.hasNextLine()){
                            line2 = scan2.nextLine();
                        }

                        if(line1.equals("")){
                            int lineCount = 0;
                            while(lineCount < header1.size()){
                                this.content += "NULL";
                                lineCount++;
                                if(lineCount < header1.size()){
                                    this.content += "~";
                                }
                            }
                        }else {
                            this.content = line1;
                        }

                    //  This will be for table 2 content. Will be different, need to check for null header position to not include column
                        if(line2.equals("")){
                            int lineCount = 0;
                            while(lineCount < header2.size()){
                              if(header2.get(lineCount) != null) {
                                  this.content += "NULL";
                                  lineCount++;
                                  if (lineCount < header2.size()) {
                                      this.content += "~";
                                  }
                              }else{
                                  lineCount++;
                              }
                            }
                        }else {

                            //line two is not blank, and checks for already printed lines

                            int lineCount = 0;

                            boolean match = false;

                            Scanner lineFind = new Scanner(file2);

                            //skips header
                            lineFind.nextLine();


                            while(lineFind.hasNext()) {

                                String checkLine = lineFind.nextLine();

                                if(!foundLines.contains(lineCount)) {
                                    if (checkMatch(header1, header2, line1, checkLine)) {

                                        line2 = checkLine;
                                        //adds to the list of already written lines
                                        foundLines.add(lineCount);
                                    }
                                }

                                lineCount++;
                            }

                            Scanner lineScan = new Scanner(line2);
                            lineScan.useDelimiter("~");

                            lineCount = 0;

                            //begins adding the new line content with delimiter
                            while(lineCount < newHeader2.size()){

                                if(newHeader2.get(lineCount) != null){
                                    String next = "~" + lineScan.next();
                                    this.content += next;
                                }else{

                                    lineScan.next();
                                }

                                lineCount++;
                            }
                            lineScan.close();
                        }

                        //writes to the new file with joined lines
                        //checks if this is the end of the file or not - keeps from having a blank space
                        if(scan1.hasNextLine() || scan2.hasNextLine()) {
                            out.println(this.content);
                        }else{
                            out.print(this.content);
                        }

                    }

                    out.close();
                    bw.close();

                    System.out.println("Success");

                } catch (IOException e) {
                    //exception handling left as an exercise for the reader
                }
                scan1.close();
                scan2.close();
            }catch(IOException ex){
                System.out.println("Unable to create the table");
            }

        }

        /*
        checkMatch : List<String>, List<String>, String, String -> boolean
        helper function that checks whether two lines of content are the same

         */
        private boolean checkMatch(ArrayList<String> header1, ArrayList<String> header2, String line1, String line2){

             boolean check = true;

            String content1;
            String content2;

            Scanner scan1 = new Scanner(line1);
            scan1.useDelimiter("~");

            for(String column : header1){

                content1 = scan1.next();

                Scanner scan2 = new Scanner(line2);
                scan2.useDelimiter("~");

                for(String inColumn : header2){

                    content2 = scan2.next();

                    if(column.equals(inColumn)){

                        if(!content1.equals(content2)){
                            check = false;
                        }
                    }

                }
            }

            return check;
        }

        /*
             removeDupl : List<String>, List<String> -> List<String>
             Helper function that will take two headers and make a new header without any duplicate column names
         */
        private ArrayList<String> removeDupl(ArrayList<String> header1, ArrayList<String> header2){

            ArrayList<String> newHeader2 = new ArrayList<>();

            int h2Count = 0;

            //adds header name and null if duplicate found
            while(h2Count < header2.size()){

                boolean found = false;

                for(String column : header1){

                    if(header2.get(h2Count).equals(column)){
                        found = true;
                    }
                }

                if(found){
                    newHeader2.add(h2Count, null);
                }else{
                    newHeader2.add(h2Count, header2.get(h2Count));
                }

                h2Count++;

            }

            return newHeader2;
        }

        /*
            joinHeaders : List<string>, List<string>
            Using newHeader created for header2, the builds a new header skipping over any null positions
         */
        private ArrayList<String> joinHeaders(ArrayList<String> header1, ArrayList<String> header2){

            ArrayList<String> newHeader = new ArrayList<>();

            newHeader.addAll(header1);

            for(String column : header2){
                if(column != null){
                    newHeader.add(column);
                }
            }

            return newHeader;
        }


       /*
          makeTable : List<String>, List<String> -> Void
          Creates a new tilda table of only chosen columns from the original column list
        */
        private void makeTable(ArrayList<String> columns, ArrayList<String> newColumns){

            try {
                Scanner scan = new Scanner(this.file);

                //this code will append to a file
            try(FileWriter fw = new FileWriter(this.newFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw))
             {
                 scan.nextLine();

                 while(scan.hasNextLine()){

                     ArrayList<String> row = getRow(scan.nextLine(), columns, newColumns);
                     this.content = "";
                     int count = 0;
                     for(String value : row){

                         this.content += value;

                         count++;

                         if(count < row.size()){
                             this.content += "~";
                         }
                     }

                     if(scan.hasNextLine()) {
                         out.println(this.content);
                     }else{
                         out.print(this.content);
                     }

                 }
                 out.close();
                 bw.close();

             } catch (IOException e) {
                 //exception handling left as an exercise for the reader
             }
            }catch(IOException ex){
                System.out.println("Unable to create the table");
            }
        }

        /*
           Answer : String -> void
           Creates the on screen content display of the given filename
         */
        public void Answer(String fileName){

            this.file = new File(fileName);
            this.rowCount = 0;

            try{

                Scanner scan = new Scanner(this.file);

                buildDisplay(scan);

                buildDisplayHead();

                while(scan.hasNextLine()){

                    buildDisplay(scan);
                }

            }catch(IOException ex){
                System.out.println("Table not found");
            }

            System.out.println("");
        }


    /*
       Helper function that creates the header border display ------  ------  ------
     */
    private void buildDisplayHead() {

        int spaceCount = 0;

       while(spaceCount < this.rowCount){

           for(int spaces = 0; spaces < 16; spaces++){
               if(spaces < 15){
                   System.out.print("-");
               }else{
                   System.out.print("- ");
               }
           }

           spaceCount++;

       }
            System.out.println("");
    }

        /*
            Helper function that builds a screen friendly display from a given tilda table file
         */
        private void buildDisplay(Scanner scan) {

                    Scanner row = new Scanner(scan.nextLine());
                    row.useDelimiter("~");

                    while(row.hasNext()) {

                        this.rowCount++;
                        this.content = row.next();
                        //  System.out.println("content: " + this.content);
                        int spaces = this.content.length() + 1;
                        // System.out.println(spaces);

                        while (spaces <= 16) {

                            this.content += " ";
                            spaces++;
                        }

                        if(row.hasNext()) {
                            System.out.print(this.content + " ");
                        }else{
                            System.out.println(this.content + " ");
                        }
                    }

        }

        /*
           getRow : String, List<String>, List<String> -> List<String>
           helper function that will locate the correct row of a delimeter table by matching original table list with new list
           Returns the entire column contents as a List<string>
         */
        private ArrayList<String> getRow(String line, ArrayList<String> columns, ArrayList<String> newColumns){

            ArrayList<String> row = new ArrayList<>();

            int columnCount = 0;

            while(columnCount < newColumns.size()){

                Scanner delim = new Scanner(line);
                delim.useDelimiter("~");

                boolean found = false;
                int count = 0;

                while(!found && count < columns.size()){

                    //Checks if newColumn header matches original column header
                    if(columns.get(count).equals(newColumns.get(columnCount))){
                        row.add(delim.next());
                        found = true;
                    }else{
                        if(delim.hasNext()) {
                            delim.next();
                        }
                    }
                    count++;
                }

                columnCount++;
            }

            return row;
        }


        /*
           Helper function when given two List<String>, returns if contents of second list are in the first.
         */
        private boolean check(ArrayList<String> columns, ArrayList<String> newColumns){

            boolean check = true;

            int checkCount = 0;

            boolean done = false;

            while(checkCount < newColumns.size() && !done){

                boolean found = false;

                int columnCount = 0;

                while(columnCount < columns.size()){

                    if(newColumns.get(checkCount).equals(columns.get(columnCount))){
                        found = true;
                    }

                    columnCount++;

                }

                if(!found){
                    check = false;
                    done = true;
                }

                checkCount++;
            }

            return check;

        }

      /*
         getSelected : String -> ArrayList<String>
         Returns a ArrayList containing Selected Columns for a table
       */
       private ArrayList<String> getSelected(String select){

           ArrayList<String> selected = new ArrayList<>();

               Scanner scan = new Scanner(select);

               scan.useDelimiter(",");

               while (scan.hasNext()) {
                   selected.add(scan.next().toUpperCase());
               }

               scan.close();

           return selected;
       }

       /*
          getColumns -> ArrayList<String>
          Returns an Array list of the column names of a table in a file
        */
       private ArrayList<String> getColumns(File _file){

           ArrayList<String> columns = new ArrayList<>();

           try {
               Scanner scan = new Scanner(_file);
               String firstRow = scan.nextLine();

               scan = new Scanner(firstRow);
               scan.useDelimiter("~");

               while (scan.hasNext()) {
                   columns.add(scan.next().toUpperCase());
               }

               scan.close();

           }catch(IOException ex){
               System.out.println("File not found");
           }

           return columns;
       }


       //Adds filenames created with program to a log file so they can be deleted
        private void writeLog(String fileName){

                    Scanner scan = new Scanner(fileName);

                    //this code will append to a file
                    try(FileWriter fw = new FileWriter("logFile.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        PrintWriter out = new PrintWriter(bw)) {

                        out.println(scan.nextLine());

                        scan.close();
                        out.close();
                        bw.close();

                    } catch (IOException e) {

                    }

        }


       //This function needs to be finished. For some reason, it will not delete filenames using found methods
        public void cleanUp()
        {
            File file = new File("logFile.txt");

            try{
                Scanner scan = new Scanner(file);

                while(scan.hasNextLine()){

                    String newFile = scan.nextLine();

                    System.out.println("Looking for :" + newFile);
                    File nFile = new File(newFile);

                    String path = nFile.getPath();

                    nFile = new File(path);

                        if(nFile.exists()){
                            System.out.println("File exists, try to delete");

                            System.out.println("Deleting " + newFile);

                            if(nFile.delete())
                            {
                                System.out.println("File deleted successfully");
                            }
                            else
                            {
                                System.out.println("Failed to delete the file");
                            }
                           //nFile.delete();
                        }


                  //  System.out.println("Deletion successful.");

                }

                try(FileWriter fw = new FileWriter("logFile.txt", false);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                    scan.close();
                    out.close();
                    bw.close();

                } catch (IOException e) {

                }

            }catch(IOException e){
                System.out.println("Logfile does not exist");
            }


        }


    }



