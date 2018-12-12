/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			//Runtime.getRuntime().exec("../postgresql/startPostgreSQL.sh");
			//Runtime.getRuntime().exec("../postgresql/createPostgreDB.sh");

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			

			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	public static boolean isNumber(String input) {
		try {
			int d = Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	} 
	public static void AddCustomer(MechanicShop esql){//1
		String input;
		try {
			String query = "INSERT INTO Customer VALUES (nextval(\'seq_customer_id'),'";
			System.out.println("To add new customer into database input the following customer information");
			do {
				System.out.print("Auto generate customer ID? (y/n): ");
				input = in.readLine();
				System.out.println("input: " + input);
				if(input.equals("y") || input.equals("n")){
					break;
				}
				System.out.println("Please enter 'y' or 'n'");
			} while (true);
			
			if (input.equals("y")) {
				//do nothing query info set correctly	
			} else if (input.equals("n")){
				query = "INSERT INTO Customer VALUES (";
				System.out.print("Please enter unique customer id: ");
				input = in.readLine();
				query += input;
				query += ", '";
			} else {
				System.out.println("Error: incorrect input parse, exiting...");
				return;
			}
			
			do {
				System.out.print("Please enter first name: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter last name: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter phone number: ");
				input = in.readLine();
				if(input.length() == 13) {
					break;
				}
				System.out.println("Format (XXX)XXX-XXXX ");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter address: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "')";

			//System.out.println(query);
			esql.executeQuery(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		String input;
		try {
			String query = "INSERT INTO MECHANIC VALUES (nextval('seq_mechanic_id'),'";
			System.out.println("To add new mechanic into database input the following mechanic information");
			do {
				System.out.print("Auto generate mechanic ID? (y/n): ");
				input = in.readLine();
				System.out.println("input: " + input);
				if(input.equals("y") || input.equals("n")){
					break;
				}
				System.out.println("Please enter 'y' or 'n'");
			} while (true);
			
			if (input.equals("y")) {
				//do nothing query info set correctly
			} else if (input.equals("n")){
				query = "INSERT INTO Mechanic VALUES (";
				System.out.print("Please enter unique mechanic id: ");
				input = in.readLine();
				query += input;
				query += ", '";
			} else {
				System.out.println("Error: incorrect input parse, exiting...");
				return;
			}
			
			do {
				System.out.print("Please enter first name: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter last name: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "',";
			
			do {
				System.out.print("Please enter years of experience: ");
				input = in.readLine();
				if (isNumber(input) && input.length() > 0) {
					break;
				}
				System.out.println("Please enter years as a single number");
			} while (true);
			query += input;
			query += ")";

			//System.out.println(query);
			esql.executeQuery(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		String input;
		try {
			String query = "INSERT INTO CAR VALUES ('";
			System.out.println("To add new car into database input the following vehicle information");
			
			do {
				System.out.print("Please enter vehicle identification number (VIN): ");
				input = in.readLine();
				if (input.length() > 0 && input.length() <= 16) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter vehicle make: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "','";
			
			do {
				System.out.print("Please enter vehicle model: ");
				input = in.readLine();
				if (input.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += input;
			query += "',";
			
			do {
				System.out.print("Please enter vehicle year: ");
				input = in.readLine();
				if (input.length() == 4 && isNumber(input)) {
					break;
				}
				System.out.println("Format: XXXX");
			} while (true);
			query += input;
			query += ")";

			//System.out.println(query);
			esql.executeQuery(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		String input, query, lname, fname, vin, cID;
		String fullNameSearch = "";
		try{
			query = "SELECT C.lname FROM Customer C WHERE C.lname = '";
			System.out.print("Inserting service request for customer with last name: ");
			do {
				lname = in.readLine();
				System.out.println();
				if (lname.length() > 0) {
					break;
				}
				System.out.println("Please enter customer last name: ");
			} while (true);
			query += lname;
			query += "';";
			
			int numCustomers = esql.executeQueryAndPrintResult(query);
			if(numCustomers == 0) {
				do {
					System.out.print("Customer last name not in database, add new customer? (y/n) ");
					input = in.readLine();
					if(input.equals("y") || input.equals("n")){
						break;
					}
					System.out.println("Please enter 'y' or 'n'");
				} while (true);
			
				if(input.equals("n")) {
					return;
				}else if (input.equals("y")) {
					AddCustomer(esql);
					InsertServiceRequest(esql);
				}
			}else if (numCustomers == 1) {
				System.out.println("Listing all cars owned by " + lname);
				query = "SELECT K.vin, K.make, K.model, K.year FROM Owns O, Customer C, Car K WHERE C.id = O.customer_id AND K.vin = O.car_vin AND C.lname = '" + lname + "';";
				fullNameSearch = "SELECT O.car_vin FROM Owns O, Customer C WHERE O.customer_id = C.id AND C.lname = '" + lname;
				esql.executeQueryAndPrintResult(query);
			}else if (numCustomers > 1) {
				System.out.println("Multiple persons with " + lname + " as last name");
				
				System.out.print("Please enter first name of customer: ");
				do {
					fname = in.readLine();
					System.out.println();
					if (fname.length() > 0) {
						break;
					}
					System.out.println("First name cannot be empty");
				} while (true);
				System.out.println("Listing all cars owned by " + fname + " " + lname);
				query = "SELECT K.vin, K.make, K.model, K.year FROM Owns O, Customer C, Car K WHERE C.id = O.customer_id AND K.vin = O.car_vin AND C.lname = '" + lname + "' AND C.fname = '" + fname + "';";
				fullNameSearch = "SELECT O.car_vin FROM Owns O, Customer C WHERE O.customer_id = C.id AND C.lname = '" + lname + "' AND C.fname = '" + fname;
				esql.executeQueryAndPrintResult(query);
			}else {
				System.out.println("ERROR: negative number of customers ...exiting");
				return;
			}
			do {
				System.out.print("Please enter vehicle identification number (VIN) of car for service request: ");
				vin = in.readLine();
				if (vin.length() == 16) {
					break;
				}
				System.out.println("Please enter 16 digit VIN");
			} while (true);
			
			do {
				System.out.print("Please enter customer ID: ");
				cID = in.readLine();
				if (isNumber(cID) && cID.length() > 0) {
					break;
				}
				System.out.println("Please enter integers only");
			} while (true);
			
			int isCarInDB = esql.executeQueryAndPrintResult(fullNameSearch + "' AND O.car_vin = '" + vin + "';");
			if(isCarInDB == 0) {
				System.out.println("VIN entered for car not in DB");
				AddCar(esql);
				query = "INSERT INTO Owns VALUES (nextval(\'seq_ownership_id'), " + cID + ", " + vin + ");";
				esql.executeQueryAndPrintResult(query);
			}
			System.out.println("Adding service request for car VIN: " + vin);
			query = "INSERT INTO Service_Request VALUES (nextval(\\'seq_rid_id'), " + cID + ", " + vin + ", ";
			
			System.out.print("Please enter date: ");
			input = in.readLine();
			query += input;
			query += ", ";
			
			System.out.print("Please enter odometer reading: ");
			input = in.readLine();
			query += input;
			query += ", '";			
			
			System.out.print("Please enter complaint: ");
			input = in.readLine();
			query += input;
			query += "';";
			
			esql.executeQueryAndPrintResult(query);
			
		}catch(Exception e){
			System.err.println (e.getMessage());
		}	
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try{
			String query = "SELECT C.date, C.comment, C.bill FROM Closed_Request C WHERE C.bill<100";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println (e.getMessage());
		}	
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try{
			System.out.println("The following customers own more than 20 cars");
			String query = "SELECT C.fname, C.lname FROM Customer C, Owns O WHERE C.id=O.customer_id GROUP BY C.id HAVING COUNT(C.id)>20";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
			System.out.println("The following cars were build before 1995 and have less than 50000 miles");
			String query = "SELECT C.make, C.model, C.year FROM Car C, Service_Request S WHERE C.vin=S.car_vin AND C.year<1995 AND S.odometer<50000";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		try{
			String query = "SELECT C.make, C.model, M.N FROM Car C, (SELECT COUNT(S.rid) AS N, S.car_vin FROM Service_Request S GROUP BY S.car_vin) AS M WHERE M.car_vin=C.vin ORDER BY M.N DESC LIMIT ";
			int input = 0;
			do {
				System.out.print("Please enter a number larger than 0: ");
				try { // read the integer, parse it and break.
					input = Integer.parseInt(in.readLine());
					continue;
				}catch (Exception e) {
					System.out.println("Your input is invalid!");
					continue;
				}//end try
			}while (input<1);
			query += input;

			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println (e.getMessage());
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		try{
			String query = "SELECT C.fname, C.lname, M.N FROM Customer C, (SELECT SUM(R.bill) AS N, S.customer_id FROM Service_Request S, Closed_Request R WHERE S.rid=R.rid GROUP BY S.customer_id) AS M WHERE M.customer_id=C.id ORDER BY M.N DESC";
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println (e.getMessage());
		}
		
	}
	
}
