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
	public static boolean compareDates(String dateOpened, String dateClosed) {
		String mO, dO, yO, mC, dC, yC;
		int imO, idO, iyO, imC, idC, iyC;
		
		String dateO = dateOpened; String dateC = dateClosed;
		
		mO = dateO.substring(0,2);
		if (mO.charAt(1) == '/') {        //"1/"
			mO = "0" + mO.substring(0,1); //"1/" -> "01"
			dateO = dateO.substring(2);   //0/2/3456 -> 2/3456 ...
		}else if (mO.charAt(2) == '/' ) { //"10/"
			mO = mO.substring(0,2); //	  "10" -> "10"
			dateO = dateO.substring(3);   //10/2/3456 -> 2/3456 ...			
		}
		
		dO = dateO.substring(0,2);
		if (dO.charAt(1) == '/') {        //"1/"
			dO = "0" + dO.substring(0,1); //"1/" -> "01"
			dateO = dateO.substring(2);   //2/3456 -> 3456 ...
		}else if (dO.charAt(2) == '/' ) { //"10/"
			dO = dO.substring(0,2); //	  "10" -> "10"
			dateO = dateO.substring(3);   //10/3456 -> 3456 ...			
		}
		
		yO = dateO.substring(0, 5);
		
		mC = dateC.substring(0,2);
		if (mC.charAt(1) == '/') {        //"1/"
			mC = "0" + mC.substring(0,1); //"1/" -> "01"
			dateC = dateC.substring(2);   //0/2/3456 -> 2/3456 ...
		}else if (mC.charAt(2) == '/' ) { //"10/"
			mC = mC.substring(0,2); //	  "10" -> "10"
			dateC = dateC.substring(3);   //10/2/3456 -> 2/3456 ...			
		}
		
		dC = dateC.substring(0,2);
		if (dC.charAt(1) == '/') {        //"1/"
			dC = "0" + dC.substring(0,1); //"1/" -> "01"
			dateC = dateC.substring(2);   //2/3456 -> 3456 ...
		}else if (dC.charAt(2) == '/' ) { //"10/"
			dC = dC.substring(0,2); //	  "10" -> "10"
			dateC = dateC.substring(3);   //10/3456 -> 3456 ...			
		}
		
		yC = dateC.substring(0, 5);
		
		imO = Integer.parseInt(mO);	imC = Integer.parseInt(mC);
		idO = Integer.parseInt(dO);	idC = Integer.parseInt(dC);
		iyO = Integer.parseInt(yO);	iyC = Integer.parseInt(yC);
		
		if (iyC > iyO) {
			return true;
		} else if (iyC == iyO) {
			if (imC > imO) {
				return true;
			}else if (imC == imO) {
				if(idC > idO) {
					return true;
				}else if (idC == idO) { //same date check the time
					System.out.println("Warning: Service request opened and closed same day...");
				}else {
					return false;
				}
			}else {
				return false;
			}
		}else {
			return false;
		}
		
		return false;
	}
	public static void AddCustomer(MechanicShop esql){//1
		String input, fname, lname, phone, address;
		try {
			String query = "INSERT INTO Customer VALUES (nextval('seq_customer_id'),'";
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
				fname = in.readLine();
				if (fname.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += fname;
			query += "','";
			
			do {
				System.out.print("Please enter last name: ");
				lname = in.readLine();
				if (lname.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += lname;
			query += "','";
			
			do {
				System.out.print("Please enter phone number: ");
				phone = in.readLine();
				if(phone.length() == 13) {
					break;
				}
				System.out.println("Format (XXX)XXX-XXXX ");
			} while (true);
			query += phone;
			query += "','";
			
			do {
				System.out.print("Please enter address: ");
				address = in.readLine();
				if (address.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += address;
			query += "')";
			
			System.out.println("------------Adding New Customer into Database-------------");
			System.out.println("Name: " + fname + " " + lname);
			System.out.println("Phone: " + phone);
			System.out.println("Address: " + address);
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		String input, fname, lname, experience;
		try {
			String query = "INSERT INTO MECHANIC VALUES (4('seq_mechanic_id'),'";
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
				fname = in.readLine();
				if (fname.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += fname;
			query += "','";
			
			do {
				System.out.print("Please enter last name: ");
				lname = in.readLine();
				if (lname.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += lname;
			query += "',";
			
			do {
				System.out.print("Please enter years of experience: ");
				experience = in.readLine();
				if (isNumber(experience) && experience.length() > 0) {
					break;
				}
				System.out.println("Please enter years as a single number");
			} while (true);
			query += experience;
			query += ")";
			
			System.out.println("------------Adding New Mechanic into Database-------------");
			System.out.println("Name: " + fname + " " + lname);
			System.out.println("Years Experience: " + experience);
			
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		String input, vin, make, model, year;
		try {
			String query = "INSERT INTO CAR VALUES ('";
			System.out.println("To add new car into database input the following vehicle information");
			
			do {
				System.out.print("Please enter vehicle identification number (VIN): ");
				vin = in.readLine();
				if (vin.length() == 16) {
					break;
				}
				System.out.println("Please enter 16 digit VIN");
			} while (true);
			query += vin;
			query += "','";
			
			do {
				System.out.print("Please enter vehicle make: ");
				make = in.readLine();
				if (make.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += make;
			query += "','";
			
			do {
				System.out.print("Please enter vehicle model: ");
				model = in.readLine();
				if (model.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += model;
			query += "', ";
			
			do {
				System.out.print("Please enter vehicle year: ");
				year = in.readLine();
				if (year.length() == 4 && isNumber(year)) {
					break;
				}
				System.out.println("Format: XXXX");
			} while (true);
			query += year;
			query += ")";

			System.out.println("------------Adding Following Car into Database------------");
			System.out.println("VIN: " + vin);
			System.out.println("Make: " + make);
			System.out.println("Model: " + model);
			System.out.println("Year: " + year);
			
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		String input, query, lname, fname, vin, date, odometer, complain;
		String cID = "";
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
					AddCustomer(esql, lname);
					InsertServiceRequest(esql);
					return;
				}
			}else if (numCustomers == 1) {
				System.out.println("Listing all cars owned by " + lname);
				query = "SELECT K.vin, K.make, K.model, K.year FROM Owns O, Customer C, Car K WHERE C.id = O.customer_id AND K.vin = O.car_vin AND C.lname = '" + lname + "';";
				fullNameSearch = "SELECT O.car_vin FROM Owns O, Customer C WHERE O.customer_id = C.id AND C.lname = '" + lname;
				esql.executeQueryAndPrintResult(query);
				cID = esql.executeQueryAndReturnResult("SELECT C.id AS cID FROM Customer C WHERE C.lname = '" + lname + "';").get(0).get(0);
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
				cID = esql.executeQueryAndReturnResult("SELECT C.id AS cID FROM Customer C WHERE C.lname = '" + lname + "' AND C.fname = '" + fname + "';").get(0).get(0);
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
			
			int isCarOwnedByUser = esql.executeQueryAndPrintResult(fullNameSearch + "' AND O.car_vin = '" + vin + "';");
			List<List<String>> carAlredyHaveOwner = esql.executeQueryAndReturnResult("SELECT C.fname, C.lname FROM Customer C, Owns O WHERE C.id = O.customer_id AND O.car_vin = '" + vin + "';");
			int isCarInDB = esql.executeQuery("SELECT C.vin FROM Car C WHERE C.vin = '" + vin + "';");
			if(isCarInDB == 0) { //car is not in car database at ALL
				System.out.println("Car with VIN " + vin + " not in DB");
				AddCar(esql, vin, lname, cID);
				//query = "INSERT INTO Owns VALUES (nextval(\'seq_ownership_id\'), " + cID + ", " + vin + ");";
				//esql.executeQueryAndPrintResult(query);
				//System.out.println("we got here!");
			}else if (isCarInDB == 1) { //car in car database
				if(isCarOwnedByUser == 0) { //not owned by you
					if(carAlredyHaveOwner.isEmpty()) { //but car has no owner
						System.out.print("Car with VIN: " + vin + "has no owner registered. Are you the owner? (y/n): ");
						do {
							input = in.readLine();
							if(input.equals("y") || input.equals("n")){
								break;
							}
							System.out.println("Please enter 'y' or 'n'");
						} while (true);
						
						if (input.equals("y")) {
							System.out.print("For security reasons please enter confirm your last name: ");
							int attempt = 3;
							do {
								input = in.readLine();
								if(input.equals(lname)){
									break;
								}
								attempt = attempt - 1;
								System.out.println("Incorrect last name! You have " + attempt + " trie(s) remaining before you are logged out...");
								if (attempt <= 0) {
									return;
								}
							} while (true);
							query = "INSERT INTO Owns VALUES (nextval('seq_ownership_id'), " + cID + ", " + vin + ");";
							esql.executeUpdate(query);
							System.out.println("The car is now registered under your name!");
						} else if (input.equals("n")){
							System.out.println("Sorry! It is illegal issue a service request for someone else's car!");
							return;
						} else {
							System.out.println("Error: incorrect input parse, exiting...");
							return;
						}
					}else { //but car has owner
						System.out.println("Sorry! It is illegal issue a service request for someone else's car!");
						System.out.println("VIN: " + vin + " is owned by " + carAlredyHaveOwner.get(0).get(0).replace(" ", "") + " " + carAlredyHaveOwner.get(0).get(1).replace(" ", ""));
						return;
					}
				}else if (isCarOwnedByUser == 1) {
							//do nothing until we need to issue service requests
				}else {
					System.out.println("Error: incorrect input parse, exiting...");
					return;
				}
			}else {
				System.out.println("Error: incorrect input parse, exiting...");
				return;
			}
			
			//---ACTUALLY ISSUING THE SERVICE REQUEST----
			
			System.out.println("Adding service request for car VIN: " + vin);
			query = "INSERT INTO Service_Request VALUES (nextval('seq_rid_id'), " + cID + ", '" + vin + "', '";
			System.out.print("Please enter date service request was opened (XX/XX/XXXX): ");
			do {
				date = in.readLine();
				System.out.println();
				if (date.length() == 10 && date.charAt(2) == '/' && date.charAt(5) == '/') {
					break;
				}
				System.out.print("Please enter valid date format (XX/XX/XXXX): ");
			} while (true);
			query += date;
			query += "', ";
			
			System.out.print("Please enter odometer reading: ");
			odometer = in.readLine();
			query += odometer;
			query += ", '";			
			
			System.out.print("Please enter complaint: ");
			complain = in.readLine();
			query += complain;
			query += "');";

			System.out.println("-------------Opening following service request-------------");
			System.out.println("Customer ID: " + cID);
			System.out.println("Car VIN:" + vin);
			System.out.println("Date Opened: " + date);
			System.out.println("Odometer:" + odometer);
			System.out.println("Complaint: " + complain);
			
			esql.executeUpdate(query);
			
		}catch(Exception e){
			System.err.println (e.getMessage());
		}	
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		String input, fquery, query, sNum, dateClosed, dateOpened, eID, bill;
		int isValid = 0;
		try{
			fquery = "INSERT INTO Closed_Request VALUES (nextval('seq_win_id'), ";
			System.out.print("Please enter service request number of request to close: ");
			do {
				sNum = in.readLine();
				System.out.println();
				query = "SELECT S.rid FROM Service_Request S WHERE S.rid = " + sNum + ";";
				isValid = esql.executeQuery(query);
				if (sNum.length() > 0 && isNumber(sNum) && isValid == 1) {
					break;
				}
				if (isValid == 0) {
					System.out.println("Invalid service request number");
				}
				System.out.println("Please enter exsisting service request number: ");
			} while (true);
			fquery += sNum + ", ";
			
			System.out.print("Please enter employee ID of employee assigned to request: ");
			do {
				eID = in.readLine();
				System.out.println();
				query = "SELECT M.id FROM Mechanic M WHERE M.id = " + eID + ";";
				isValid = esql.executeQuery(query);
				if (sNum.length() > 0 && isNumber(sNum) && isValid == 1) {
					break;
				}
				if (isValid == 0) {
					System.out.println("Invalid employee ID");
				}
				System.out.println("Please enter valid employee ID: ");
			} while (true);
			fquery += eID + ", '";
			
			System.out.print("Please enter date service request " + sNum + " was closed (XX/XX/XXXX): ");
			do {
				do {
					dateClosed = in.readLine();
					System.out.println();
					if (dateClosed.length() == 10 && dateClosed.charAt(2) == '/' && dateClosed.charAt(5) == '/') {
						break;
					}
					System.out.print("Please enter valid date format (XX/XX/XXXX): ");
				} while (true);
				dateOpened = esql.executeQueryAndReturnResult("SELECT S.date FROM Service_Request S WHERE S.rid = " + sNum + ";").get(0).get(0);
				boolean dateClosedCorrect = compareDates(dateOpened, dateClosed);
				if (dateClosed.length() > 0 && dateClosedCorrect == true) {
					break;
				}
				if (dateClosedCorrect == false) {
					System.out.println("Please enter date after service request was opened");
				}
			} while (true);
			fquery += dateClosed + " 12:00:00 AM', '";
			
			System.out.print("Please enter any comments here: ");
			input = in.readLine();
			fquery += input + "', ";
			
			System.out.print("Please enter final bill ammout for service: $");
			do {
				bill = in.readLine();
				System.out.println();
				if (isNumber(bill) && bill.length() > 0 && !bill.contains("-")) {
					break;
				}
				if (bill.contains("-")) {
					System.out.println("Final bill cannot be negative number!");
				}
				System.out.println("Please enter valid bill: $");
			} while (true);
			fquery += bill + ");";
			
			System.out.println("---------Creating following closed service request---------");
			System.out.println("Service Request ID: " + sNum);
			System.out.println("Mechanic ID:" + eID);
			System.out.println("Date Closed: " + dateClosed);
			System.out.println("Comments:" + input);
			System.out.println("Bill: $" + bill);
			esql.executeUpdate(fquery);

		}catch(Exception e){
			System.err.println (e.getMessage());
		}
			
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
	
	public static void AddCar(MechanicShop esql, String vin, String lname, String cID){//3
		String input, make, model, year;
		try {
			String query = "INSERT INTO Car VALUES ('";
			System.out.println("To add car, confirm VIN and input the rest of the vehicle's information");
			
			do {
				System.out.print("Is this the correct vehicle identification number (VIN): " + vin + " (y/n)? ");
				input = in.readLine();
				if(input.equals("y") || input.equals("n")){
					break;
				}
				System.out.println("Please confirm 'y' or 'n'");
			} while (true);
			
			if (input.equals("y")) {
				query += vin + "', '";	
			} else if (input.equals("n")){
				do {
					System.out.print("Restart entering vehicle information for " + lname + "'s new car (y/n)? ");
					input = in.readLine();
					if(input.equals("y") || input.equals("n")){
						break;
					}
					System.out.println("Please confirm 'y' or 'n'");
				} while (true);
				
				if (input.equals("y")) {
					do {
						System.out.print("Please re-enter vehicle identification number (VIN): ");
						input = in.readLine();
						if (input.length() == 16) {
							query += input;
							query += "','";
							break;
						}
						System.out.println("Please enter 16 digit VIN");
					} while (true);
				} else if (input.equals("n")){
					return;
				} else {
					System.out.println("Error: incorrect input parse, exiting...");
					return;
				}
			} else {
				System.out.println("Error: incorrect input parse, exiting...");
				return;
			}
					
			do {
				System.out.print("Please enter vehicle make: ");
				make = in.readLine();
				if (make.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += make;
			query += "', '";
			
			do {
				System.out.print("Please enter vehicle model: ");
				model = in.readLine();
				if (model.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += model;
			query += "',";
			
			do {
				System.out.print("Please enter vehicle year: ");
				year = in.readLine();
				if (year.length() == 4 && isNumber(year)) {
					break;
				}
				System.out.println("Format: XXXX");
			} while (true);
			query += year;
			query += ")";
			
			System.out.println("------------Adding " + lname + "'s Car into Database------------");
			System.out.println("VIN: " + vin);
			System.out.println("Make: " + make);
			System.out.println("Model: " + model);
			System.out.println("Year: " + year);
			
			esql.executeUpdate(query);
			
			query = "INSERT INTO Owns VALUES (nextval('seq_ownership_id'), " + cID + ", '" + vin + "');";
			esql.executeUpdate(query);
			
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	public static void AddCustomer(MechanicShop esql, String lname){//1
		String input, fname, phone, address;
		try {
			String query = "INSERT INTO Customer VALUES (nextval('seq_customer_id'),'";
			System.out.println("To add new customer " + lname + " into database input the following customer information");
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
				fname = in.readLine();
				if (fname.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += fname;
			query += "','";
			

			do {
				System.out.print("Is " + lname + " the correct last name of " + query + " (y/n)? ");
				input = in.readLine();
				if(input.equals("y") || input.equals("n")){
					break;
				}
				System.out.println("Please confirm 'y' or 'n'");
			} while (true);
			
			if (input.equals("y")) {
				query += lname + "','";	
			} else if (input.equals("n")){
				do {
					System.out.print("Enter correct last name for customer (y/n)? ");
					input = in.readLine();
					if(input.equals("y") || input.equals("n")){
						break;
					}
					System.out.println("Please confirm 'y' or 'n'");
				} while (true);
				
				if (input.equals("y")) {
					do {
						System.out.print("Please re-enter customer last name: ");
						input = in.readLine();
						if (input.length() > 0) {
							query += input;
							query += "','";
							break;
						}
						System.out.println("Cannot leave field blank");
					} while (true);
				} else if (input.equals("n")){
					return;
				} else {
					System.out.println("Error: incorrect input parse, exiting...");
					return;
				}
			} else {
				System.out.println("Error: incorrect input parse, exiting...");
				return;
			}
			
			do {
				System.out.print("Please enter phone number: ");
				phone = in.readLine();
				if(phone.length() == 13) {
					break;
				}
				System.out.println("Format (XXX)XXX-XXXX ");
			} while (true);
			query += phone;
			query += "','";
			
			do {
				System.out.print("Please enter address: ");
				address = in.readLine();
				if (address.length() > 0) {
					break;
				}
				System.out.println("Cannot leave field blank");
			} while (true);
			query += address;
			query += "')";
			
			System.out.println("------------Adding New Customer into Database-------------");
			System.out.println("Name: " + fname + " " + lname);
			System.out.println("Phone: " + phone);
			System.out.println("Address: " + address);
			
			esql.executeUpdate(query);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
