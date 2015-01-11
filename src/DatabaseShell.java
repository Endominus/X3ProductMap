import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
			GenerateFactories();
			GenerateSectors();
			
			//statement.executeUpdate("insert into ware values(0, 'Energy Cells', 14, 1)");
			//statement.executeUpdate("insert into ware values(1, 'Energy Cellsa', 14, 0)");
			
			/*ResultSet rs = statement.executeQuery("select * from sector");
			while (rs.next())
			{
				// read the result set
				System.out.println("name = " + rs.getString("name"));
				System.out.println("id = " + rs.getInt("id"));
				System.out.println("coords = " + rs.getInt("x") + "," + rs.getInt("y"));
			}/**/
			
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

	private static void GenerateSectors() throws SQLException, IOException
	{
		String name, line, x, y, id, factid, race, size, yield;
		FileReader fin = new FileReader("res/X3_sectors.txt");
		Scanner scanner = new Scanner(fin);
		line = scanner.nextLine();
		
		//while (scanner.hasNextLine())
		while (!line.equals("..."))
		{
			id = line.split(":")[1];
			name = scanner.nextLine().split(":")[1];
			line = scanner.nextLine().split(":")[1];
			x = line.split(",")[0];
			y = line.split(",")[1];
			
			statement.executeUpdate(String.format("insert into sector values(%1s, '%2s', %3s, %4s)", id, name, x, y));
			
			scanner.nextLine();
			line = scanner.nextLine();
			
			while (!line.equals("linked_sectors:"))
			{
				yield = "0";
				factid = line.split(":")[1];
				race = scanner.nextLine().split(":")[1];
				size = scanner.nextLine().split(":")[1];
				
				line = scanner.nextLine();
				if (line.split(":")[0].equals("\tyield"))
				{
					yield = line.split(":")[1];
					line = scanner.nextLine();
				}
				
				statement.executeUpdate(String.format("insert into sectorcontent values(%1s, %2s, '%3s', '%4s', %5s)", id, factid, race, size, yield));
				//System.out.println(line);
			}
			
			ResultSet rs = statement.executeQuery("select sect1id from sectorlink where sect2id = " + id);
			ArrayList<String> results = new ArrayList<>();
			while (rs.next())
			{
				results.add(rs.getString(1));
			}
			line = scanner.nextLine();
			
			while (!line.equals("---"))
			{
				line = line.split(":")[1];
				
				if (!results.contains(line))
				{
					statement.executeUpdate(String.format("insert into sectorlink values(%1s, %2s, 1)", id, line));
				}
				
				line = scanner.nextLine();
				//System.out.println(line);
			}
			
			line = scanner.nextLine();
		}
		
		scanner.close();
		fin.close();
		
	}

	private static void GenerateFactories() throws FileNotFoundException,
			SQLException, IOException
	{
		String name, line, id, wareid, wareamount, size;
		
		FileReader fin = new FileReader("res/X3_factories.txt");
		Scanner scanner = new Scanner(fin);
		line = scanner.nextLine();
		
		while (!line.equals("..."))
		{
			id = line.split(":")[1];
			name = scanner.nextLine().split(":")[1];
			size = scanner.nextLine().split(":")[1];
			
			statement.executeUpdate(String.format("insert into factory values(%1s, '%2s', '%3s')", id, name, size));

			line = scanner.nextLine();
			line = scanner.nextLine();
			while (!line.equals("---"))
			{
				wareid = line.split(":")[1];
				wareamount = scanner.nextLine().split(":")[1];
				
				statement.executeUpdate(String.format("insert into factoryio values(%1s, %2s, %3s)", id, wareid, wareamount));
				
				line = scanner.nextLine();
			}

			line = scanner.nextLine();
		}

		scanner.close();
		fin.close();
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
				.executeUpdate("create table sector (id integer primary key, name text, x integer, y integer)");
		statement
				.executeUpdate("create table factoryio (factoryid integer, wareid integer, amount real, foreign key (factoryid) references factory(id), foreign key (wareid) references ware(id))");
		statement
				.executeUpdate("create table sectorlink (sect1id integer, sect2id integer, distance integer, foreign key (sect1id) references sector(id), foreign key (sect2id) references sector(id))");
		statement
				.executeUpdate("create table sectorcontent (sectid integer, factid integer, race text, size text, yield real, foreign key (sectid) references sector(id), foreign key (factid) references factory(id))");
	}

}
