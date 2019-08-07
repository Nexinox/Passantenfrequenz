package de.hofmann.Passantenfrequenz.XLS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@WebListener
public class ContextListener implements ServletContextListener{

	Timer timer = new Timer();
	private boolean stopping = false;
    static Statement stmt;
    static Connection c;
    int proccesing = 0;
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
               .getConnection("jdbc:postgresql://localhost:5432/overlay",
               "postgres", "Ac062002");
            stmt = c.createStatement();
        } catch (Exception e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
         }
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				
				timer.scheduleAtFixedRate(new TimerTask() {
					
					@Override
					public void run() {
						
						checkAndUpdate();
						
					}
				}, 0, 15000);
			}
		}).run();		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("destroyed");
        try {
        	stmt.close();
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopping = true;
		timer.cancel();
	}
	
	private void checkAndUpdate() {
        System.out.println("listening");
		if (stopping) {
			timer.cancel();
		} else {
			File dir = new File("xls/");
			if (!dir.exists()) {
				dir.mkdir();
			}
			
			File[] files = dir.listFiles();
			
			if (files != null) {
				for (File file : files) {
					if (file.getName().endsWith(".xls")) {

						getEntrys(file.getName()).forEach(e ->{
							proccesEntry(e);
							
						});

						moveFileToBackup(file.getName());
					}
					
					
				}
			}
		}
	}
	

    private void proccesEntry(Entry e) {
    	
    	if (proccesing == 0) {
			System.out.println("processing");
			proccesing++;
		}
        
        String sql = "INSERT INTO data (\"CamName\",\"Date\",\"StartTime\",\"EndTime\","
        +" \"TotalIn\", \"totalout\", \"personsin\","
        +" \"personsout\", \"unknownin\", \"unknownout\") "
           + "VALUES ('"+e.getCamName()+"', TO_DATE('"+
     		   new SimpleDateFormat("dd.MM.yyyy").format(e.getDate())
     		   +"','DD.MM.YYYY'), '"+e.getStartTime()+"','"+e.getEndTime()
     		   +"',"+e.getTotalIn()+","+e.getTotalOut()+","+e.getPersonsIn()
     		   +","+e.getPersonsOut()+","+e.getUnknownIn()+","+e.getUnknownOut()+");";

        try {
			stmt.executeUpdate(sql);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private int skipFirst = 0;
    private int substingCounter = 1;
    private boolean stop;
	private Collection<Entry> getEntrys(String fileName){
		List<Entry> entrys = new ArrayList<Entry>();
        System.out.println("getering");

		
        Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File("xls/"+fileName));
		} catch (EncryptedDocumentException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (workbook == null) {
			return null;
		}
		
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        
        sheet.forEach(row -> {
        	Entry entry = new Entry();
        	
    		if (dataFormatter.formatCellValue(row.getCell(0)).equals("Datum")) {
    			stop = true;	
    		}
        	
        	if (!stop && skipFirst != 0) {
        		
                row.forEach(cell -> {
            		String cellValue = dataFormatter.formatCellValue(cell);
            		if (substingCounter == 1) {
            			entry.setCamName(cellValue);
            		}else if (substingCounter == 2) {
            			try {
            				DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
            				Date ddate;
            				String date = cellValue.substring(0, 10);
            				ddate = format.parse(date);
            				entry.setDate(ddate);
            			} catch (ParseException e) {
            				e.printStackTrace();
            			}
            		}else if (substingCounter == 3) {
            			String time = cellValue.substring(11, 19);
            			LocalTime t = LocalTime.parse(time) ;
            			entry.setEndTime(t);
            			t = t.minusHours(1);
            			entry.setStartTime(t);
            		}else if (substingCounter == 4) {
            			entry.setTotalIn(Integer.parseInt(cellValue));
            		}else if (substingCounter == 5) {
            			entry.setTotalOut(Integer.parseInt(cellValue));
            		}else if (substingCounter == 6) {
            			entry.setPersonsIn(Integer.parseInt(cellValue));
            		}else if (substingCounter == 7) {
            			entry.setPersonsOut(Integer.parseInt(cellValue));
            		}else if (substingCounter == 8) {
            			entry.setUnknownIn(Integer.parseInt(cellValue));
            		}else if (substingCounter == 9) {
            			entry.setUnknownOut(Integer.parseInt(cellValue));
            		}
            		substingCounter++;
                });	
                substingCounter = 1;
            	entrys.add(entry);
			}
        	skipFirst = 1;

        });  
        try {
			workbook.close();
			return entrys;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
        
	}
	
	private void moveFileToBackup(String fileName) {
        System.out.println("moving");
        proccesing--;
        stop = false;
        
		File file = new File("xls/"+fileName);
		File newFile = new File("backup/xls/"+fileName);
		if (!newFile.exists()) {
			newFile.mkdirs();
		}
		try {
			Files.move(file.toPath(),newFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}