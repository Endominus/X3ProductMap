import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class DatabaseShell
{
	
	private static Connection connection = null;
	private static Statement statement = null;
	
	public static void InitializeDatabase() throws ClassNotFoundException
	{
		
		Class.forName("org.sqlite.JDBC");
		try
		{
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:res/x3.db");
			statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			RefreshTables();

			GenerateWares();
			
			//statement.executeUpdate("insert into ware values(0, 'Energy Cells', 14, 1)");
			//statement.executeUpdate("insert into ware values(1, 'Energy Cellsa', 14, 0)");
			
			ResultSet rs = statement.executeQuery("select * from ware");
			while (rs.next())
			{
				// read the result set
				System.out.println("name = " + rs.getString("name"));
				//System.out.println("id = " + rs.getInt("id"));
				System.out.println("product = " + rs.getBoolean("product"));
			}
			
		} catch (SQLException | IOException e)
		{
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally
		{
			try
			{
				if (connection != null)
					connection.close();
			} catch (SQLException e)
			{
				// connection close failed.
				System.err.println(e);
			}
		}
	}

	private static void GenerateWares() throws FileNotFoundException,
			SQLException, IOException
	{
		int id, price;
		String[] data;
		FileReader fin = new FileReader("res/X3_wares.txt");
		Scanner scanner = new Scanner(fin);
		data = scanner.nextLine().split(",");
		
		//while (scanner.hasNextLine())
		while (!data[0].equals("..."))
		{
			//data = scanner.nextLine().split(",");
			
			// File currently contains wares that we won't be using
			//if (data.equals("..."))
			//	break;
			
			id = Integer.parseInt(data[0]);
			price = Integer.parseInt(data[2]);
			
			statement.executeUpdate(String.format("insert into ware values(%1d, \'%2s\', %3d, %4s)", id, data[1], price, data[3]));
			data = scanner.nextLine().split(",");
		}
		
		scanner.close();
		fin.close();
	}

	private static void RefreshTables() throws SQLException
	{
		statement.executeUpdate("drop table if exists ware");
		statement.executeUpdate("drop table if exists sector");
		statement.executeUpdate("drop table if exists factory");
		statement.executeUpdate("drop table if exists factoryio");
		statement.executeUpdate("drop table if exists sectorlink");
		statement.executeUpdate("drop table if exists sectorcontent");

		statement
				.executeUpdate("create table ware (id integer primary key, name text, price integer, product boolean)");
		statement
				.executeUpdate("create table factory (id integer primary key, name text, size text)");
		statement
				.executeUpdate("create table sector (id integer primary key, name text)");
		statement
				.executeUpdate("create table factoryio (factoryid integer, wareid integer, amount real, foreign key (factoryid) references factory(id), foreign key (wareid) references ware(id))");
		statement
				.executeUpdate("create table sectorlink (sect1id integer, sect2id integer, distance integer, foreign key (sect1id) references sector(id), foreign key (sect2id) references sector(id))");
		statement
				.executeUpdate("create table sectorcontent (sectid integer, factid integer, race text, size text, yield real, foreign key (sectid) references sector(id), foreign key (factid) references factory(id))");
	}

}
