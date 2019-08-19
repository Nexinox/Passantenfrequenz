package de.hofmann.Passantenfrequenz.XLS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import de.hofmann.Passantenfrequenz.dataacess.DatabaseClientImpl;
import de.hofmann.Passantenfrequenz.model.Entry;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
@WebListener
public class ContextListener implements ServletContextListener {

	private Timer timer = new Timer();
	private boolean stopping = false;
	private int processing = 0;
	private Date lastTime;
	private DatabaseClientImpl client;


    /**
     * Starts the Timer Loop and initializes LastTime, client
     */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
	    client = new DatabaseClientImpl();
		getLastTimeIfExists();
        new Thread(() -> timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkAndUpdate();
            }
        }, 0, 15000)).start();
	}

    /**
     * Gets the time of the last entry in Database if there are any and sets global LastTime variable
     */
	@SuppressWarnings("deprecation")
	private void getLastTimeIfExists() {

		String sql = "select * from data order by \"Date\" DESC, \"EndTime\" DESC";
		try {
			ResultSet rs = client.getBySql(sql);

			if (rs.next()) {
				Date date = rs.getDate("Date");
				Time time = rs.getTime("EndTime");
				lastTime = new Date(date.getYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(),
						time.getSeconds());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

    /**
     * Stops the Timer Loop when Application Context is destroyed
     */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("destroyed");
		stopping = true;
		timer.cancel();
	}

    /**
     * listens for file changes in xls directory every 15 seconds (modifiable)
     */
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

						Objects.requireNonNull(getEntrys(file.getName())).forEach(this::processEntry);

						moveFileToBackup(file.getName());
					}
				}
			}
		}
	}

    /**
     * @param e The Entry that is to be inserted into the Database
     *
     * Inserts the Entry into Database
     */
	@SuppressWarnings("deprecation")
	private void processEntry(Entry e) {

		if (processing == 0) {
			System.out.println("processing");
			processing++;
		}
		Date checkDate = e.getDate();
		checkDate.setHours(e.getEndTime().getHour());
		checkDate.setMinutes(e.getEndTime().getMinute());
		checkDate.setSeconds(e.getEndTime().getSecond());

		if (e.getDate().compareTo(lastTime) <= 0 && !makeTime) return;

		String sql = String.format("INSERT INTO data (\"CamName\",\"Date\",\"StartTime\",\"EndTime\", \"TotalIn\", \"totalout\", \"personsin\", \"personsout\", \"unknownin\", \"unknownout\") VALUES ('%s', TO_DATE('%s','DD.MM.YYYY'), '%s','%s',%d,%d,%d,%d,%d,%d);", e.getCamName(), new SimpleDateFormat("dd.MM.yyyy").format(e.getDate()), e.getStartTime(), e.getEndTime(), e.getTotalIn(), e.getTotalOut(), e.getPersonsIn(), e.getPersonsOut(), e.getUnknownIn(), e.getUnknownOut());

		try {
            client.writeBySql(sql);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	private int skipFirst = 0;
	private int substringCounter = 1;
	private boolean stop = false, makeTime = false;
    /**
     * @param fileName The Name of the Worksheet of which to get the entrys from
     * @return collection of Entry Objects with Entrys
     *
     * Gets all entrys from Worksheet and returns Collection of Entry Objects
     */
	@SuppressWarnings("deprecation")
	private Collection<Entry> getEntrys(String fileName) {
		List<Entry> entrys = new ArrayList<>();
		System.out.println("gathering");

		Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File("xls/" + fileName));
		} catch (EncryptedDocumentException | InvalidFormatException | IOException e) {
			e.printStackTrace();
		}

        if (workbook == null) return null;

		Sheet sheet = workbook.getSheetAt(0);
		DataFormatter dataFormatter = new DataFormatter();

        for (Row row : sheet) {
            Entry entry = new Entry();

            if (dataFormatter.formatCellValue(row.getCell(0)).equals("Datum")) stop = true;

            if (!stop && skipFirst != 0) {

                if (lastTime == null) {
                    lastTime = new Date();
                    makeTime = true;
                }

                for (Cell cell : row) {
                    String cellValue = dataFormatter.formatCellValue(cell);
                    switch (substringCounter) {
                        case 1:
                            entry.setCamName(cellValue);
                            break;
                        case 2:
                            try {
                                DateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
                                Date ddate;
                                String date = cellValue.substring(0, 10);
                                ddate = format.parse(date);
                                entry.setDate(ddate);
                                if (makeTime) {
                                    lastTime.setYear(ddate.getYear());
                                    lastTime.setMonth(ddate.getMonth());
                                    lastTime.setDate(ddate.getDate());
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            String time = cellValue.substring(11, 19);
                            LocalTime t = LocalTime.parse(time);
                            entry.setEndTime(t);
                            if (makeTime) {
                                lastTime.setHours(t.getHour());
                                lastTime.setMinutes(t.getMinute());
                                lastTime.setSeconds(t.getSecond());
                            }
                            t = t.minusHours(1);
                            entry.setStartTime(t);
                            break;
                        case 4:
                            entry.setTotalIn(Integer.parseInt(cellValue));
                            break;
                        case 5:
                            entry.setTotalOut(Integer.parseInt(cellValue));
                            break;
                        case 6:
                            entry.setPersonsIn(Integer.parseInt(cellValue));
                            break;
                        case 7:
                            entry.setPersonsOut(Integer.parseInt(cellValue));
                            break;
                        case 8:
                            entry.setUnknownIn(Integer.parseInt(cellValue));
                            break;
                        case 9:
                            entry.setUnknownOut(Integer.parseInt(cellValue));
                            break;
                    }
                    substringCounter++;
                }
                substringCounter = 1;
                entrys.add(entry);
            }
            skipFirst = 1;

        }
        try {
			workbook.close();
			return entrys;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * @param fileName The Name of the File that shall be moved to backup directory
     *
     * Moves the file to the backup directory
     */
	private void moveFileToBackup(String fileName) {
		System.out.println("moving");
		processing--;
		stop = false;
		makeTime = false;

		File file = new File("xls/" + fileName);
		File newFile = new File("backup/xls/" + fileName);
		if (!newFile.exists()) newFile.mkdirs();
		try {
			Files.move(file.toPath(), newFile.toPath(), REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
