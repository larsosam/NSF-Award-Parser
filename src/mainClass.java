import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import  java.io.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class mainClass {
	
	//Class Variables
	final private static int NUM_INVESTIGATORS = 4;
	final private static int NUM_REFERENCES = 40;
	final private static Pattern pattern = Pattern.compile(">(.*?)<");
	
	public static void main(String[] args) {
		
		//Makes sure we have valid arguments
		if (args.length != 2) {
			System.out.println("Usage: java -jar NSFAwardParser.jar [Source Folder] [Output Path]");
			System.exit(1);
		}
		
		String folderPath = args[0];
		if (!folderPath.endsWith(File.separator)) {
			folderPath += File.separator;
		}

		String wbPath = args[1];
		if (!wbPath.endsWith(File.separator)) {
			wbPath += File.separator;
		}
		
		//If output file exists, looks if you want to overwrite
		if (new File(wbPath + "Database.xlsx").exists() || new File(wbPath + "Database.xlsx").exists()) {
			System.out.println("Output file already exists. Overwrite? y/n");
			boolean hasChoice = false;
			while (hasChoice == false) {
				Scanner scnr = new Scanner(System.in);
				String choice = scnr.next();
				if (choice.equals("y")) {
					hasChoice = true;
					scnr.close();
				} else if (choice.equals("n")) {
					System.out.println("Exiting Program");
					System.exit(0);
				} else {
					System.out.println("Invalid Input. y for yes, n for no");
				}
			}
		}
		
		//The workbook that we'll be working in
	    SXSSFWorkbook wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
	    SXSSFSheet sheet = wb.createSheet();
	    
	    //Labels for the header
		String[] labels = {"Award Title", "Award Effective Date", 
				"Award Expiration Date", "Award Amount", "Award Instrument", 
				"Organization Code", "Organization Directorate", 
				"Organization Division", "Program Officer First Name", 
				"Program Officer Middle Name", "Program Officer Last Name", 
				"Abstract Narration", "Min Amd Letter Date", "Max Amd Letter Date", 
				"ARRA Amount", "Award ID", "Investigator First Name", 
				"Investigator Last Name", "Investigator Email Address", 
				"Investigator Start Date", "Investogator End Date", 
				"Investigator Role Code", "Total Investigators", "Institution Name", 
				"Institution City Name", "Institution Zip Code", "Institution Phone Number",
				"Institution Street Address", "Institution Country Name", 
				"Institution State Name", "Institution State Code", 
				"Program Element Code", "Program Element Text", 
				"Program Reference Code", "Program Reference Text"};
		
		SXSSFRow rowhead = sheet.createRow(0);
		
		//Counter for the cell number in the row
		int counter = 0;
		
		//Assigns labels to the header
		for (int i = 0; i < labels.length; i++) {
			if (i == 16) {
				
				//Loops through and makes investigator headers
				for (int j = 0; j < NUM_INVESTIGATORS; j++) {
					rowhead.createCell(counter++).setCellValue(labels[16]);
					rowhead.createCell(counter++).setCellValue(labels[17]);
					rowhead.createCell(counter++).setCellValue(labels[18]);
					rowhead.createCell(counter++).setCellValue(labels[19]);
					rowhead.createCell(counter++).setCellValue(labels[20]);
					rowhead.createCell(counter++).setCellValue(labels[21]);
				}
				i = 22;
			}
			rowhead.createCell(counter++).setCellValue(labels[i]);
		}
		
		//Loops through and makes reference headers
		for (int i = 0; i < NUM_REFERENCES; i++) {
			rowhead.createCell(counter++).setCellValue(labels[33]);
			rowhead.createCell(counter++).setCellValue(labels[34]);
		}
		rowhead.createCell(counter++).setCellValue("Number of Program References");
		
		//Starts doing work with the file

		File folder = new File(folderPath);
		File dest = new File(wbPath);
		if (!folder.exists()) {
			System.out.println("Source Folder Cannot Be Found");
			System.exit(1);
		}
		if (!dest.exists()) {
			System.out.println("Destination Path Cannot Be Found");
			System.exit(1);
		}
		System.out.println("Opening Files");
	    
		//Searches through every file and folder, adds the contents
		//to the sheet.
	    fileSearch(folder, sheet);
		
		System.out.println("Writing File");
	    try {
		    FileOutputStream out = new FileOutputStream(wbPath + "Database.xlsx");
			wb.write(out);
		    out.close();
		    System.out.println("\n" + wbPath + "Database.xlsx successfully written");
		    wb.close();
		    wb.dispose();
	    } catch (IOException e) {
			System.out.println("Should not happen. Line 108.");
		}
	}
	
	/**
	 * A recursive function that parses the file if the input is a file, 
	 * otherwise recursively calls the function again if the input is a 
	 * directory
	 * 
	 * @param file Either a file, or a directory. Does stuff if "file" is a
	 * file, otherwise recursively calls the function if input is a directory
	 * 
	 * @param sheet The sheet we're writing on
	 */
	public static void fileSearch(File file, SXSSFSheet sheet) {
		
		if (file.isFile()) {
			parseFile(file, sheet);
			
			//We're all done here
			return;
		}
		
		//The file is a directory
		else {

			//Calls function for every file inside the directory
			for (File temp : file.listFiles()) {
				fileSearch(temp, sheet);
			}
		}
	}
	
	/**
	 * Puts the contents of the file into the sheet
	 * 
	 * @param file The file we're parsing
	 * @param sheet The sheet we're writing on
	 */
	public static void parseFile(File file, SXSSFSheet sheet) {

		//Ensures we only get what we want
		if (!file.getName().endsWith(".xml")) {
			return;
		}
		SXSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.createCell(0).setCellValue(file.getName());
		
		//The line containing the information
		String line = "";
		
		//The string of information we want
		String str = "";
	
		//Number of Investigators
		ArrayList<Investigator> investigators = new ArrayList<Investigator>();
		
		//Number of program references
		ArrayList<ProgramReference> programReferences = new ArrayList<ProgramReference>();
		
		try {
			//FileReader
			BufferedReader scnr = new BufferedReader(new FileReader(file));

			while ((line = scnr.readLine()) != null) {
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					str = matcher.group(1);
				}
				if (line.contains("<AwardTitle>")) {
					row.createCell(0).setCellValue(str);
				}
				else if (line.contains("<AwardEffectiveDate>")) {
					row.createCell(1).setCellValue(str);
				}
				else if (line.contains("<AwardExpirationDate>")) {
					row.createCell(2).setCellValue(str);
				}
				else if (line.contains("<AwardAmount>")) {
					row.createCell(3).setCellValue(str);
				}
				else if (line.contains("<AwardInstrument>")) {
					while (!(line = scnr.readLine()).contains("</AwardInstrument>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<Value>")) {
							row.createCell(4).setCellValue(str);
						}
					}
				}
				else if (line.contains("<Organization>")) {
					while (!(line = scnr.readLine()).contains("</Organization>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<Code>")) {
							row.createCell(5).setCellValue(str);
						}
						else if (line.contains("<Directorate>")) {
							while (!(line = scnr.readLine()).contains("</Directorate>")) {
								
								matcher = pattern.matcher(line);
								while (matcher.find()) {
									str = matcher.group(1);
								}
								if (line.contains("<LongName>")) {
									row.createCell(6).setCellValue(str);
								}
							}
						}
						else if (line.contains("<Division>")) {
							while (!(line = scnr.readLine()).contains("</Division>")) {
								
								matcher = pattern.matcher(line);
								while (matcher.find()) {
									str = matcher.group(1);
								}
								if (line.contains("<LongName>")) {
									row.createCell(7).setCellValue(str);
								}
							}
						}
					}
				}
				else if (line.contains("<ProgramOfficer>")) {
					while (!(line = scnr.readLine()).contains("</ProgramOfficer>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<SignBlockName>")) {
							//Stores the name in chunks
							String[] tmp = str.trim().split("\\s+");
							if (tmp[0].equals("name") && tmp[1].equals("not") && tmp[2].equals("available")) {
								//Do nothing, cause they didn't put the officer name in!
							}
							else if (tmp.length == 1) {
								row.createCell(8).setCellValue(tmp[0]);
							} else if (tmp.length == 2) {
								row.createCell(8).setCellValue(tmp[0]);
								row.createCell(10).setCellValue(tmp[1]);
							} else if (tmp.length == 3) {
								row.createCell(8).setCellValue(tmp[0]);
								row.createCell(9).setCellValue(tmp[1]);
								row.createCell(10).setCellValue(tmp[2]);
							} else if (tmp.length == 4) {
								row.createCell(8).setCellValue(tmp[0]);
								row.createCell(9).setCellValue(tmp[1]);
								row.createCell(10).setCellValue(tmp[2] + " " + tmp[3]);
								
							//This aint just a name.
							} else {
								
								//Has a middle initial, use this
								if (tmp[1].length() == 1 || tmp[1].length() == 2) {
									row.createCell(8).setCellValue(tmp[0]);
									row.createCell(9).setCellValue(tmp[1]);
									row.createCell(10).setCellValue(tmp[2]);
								}
								
								//Has no middle initial
								else {
									row.createCell(8).setCellValue(tmp[0]);
									row.createCell(10).setCellValue(tmp[1]);
								}
							}
						}
					}
				}
				else if (line.contains("<AbstractNarration>")) {
					matcher = pattern.matcher(line);
					while (matcher.find()) {
						str = matcher.group(1);
					}
					row.createCell(11).setCellValue(str);
				}
				else if (line.contains("<MinAmdLetterDate>")) {
	
					row.createCell(12).setCellValue(str);
				}
				else if (line.contains("<MaxAmdLetterDate>")) {
					row.createCell(13).setCellValue(str);
				}
				else if (line.contains("<ARRAAmount>")) {
					row.createCell(14).setCellValue(str);
				}
				else if (line.contains("<AwardID>")) {
					row.createCell(15).setCellValue(str);
				}
				
				//Investigator creator loops
				else if (line.contains("<Investigator>")) {
					
					//Stores each new investigator
					Investigator temp = new Investigator();
					while (!(line = scnr.readLine()).contains("</Investigator>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<FirstName>")) {
							temp.setInvestigatorFirstName(str);
						}
						if (line.contains("<LastName>")) {
							temp.setInvestigatorLastName(str);
						}
						if (line.contains("<EmailAddress>")) {
							temp.setInvestigatorEmailAddress(str);
						}
						if (line.contains("<StartDate>")) {
							temp.setInvestigatorStartDate(str);
						}
						if (line.contains("<EndDate>")) {
							temp.setInvestigatorStartDate(str);
						}
						if (line.contains("<RoleCode>")) {
							temp.setInvestigatorRoleCode(str);
						}
					}
					if (investigators.size() == 0) {
						investigators.add(temp);
					}
					//PI at front of list
					else if (temp.getInvestigatorRoleCode().equals("Principal Investigator")) {
						investigators.add(0, temp);
					}
					
					//Co-PI just after PI
					else if (temp.getInvestigatorRoleCode().equals("Co-Principal Investigator")) {
						
						investigators.add(1, temp);
					}
					
					//Everyone else
					else {
						investigators.add(temp);
					}
				}
				
				//Creates the institution
				else if (line.contains("<Institution>")) {
					while (!(line = scnr.readLine()).contains("</Institution>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<Name>")) {
							row.createCell(41).setCellValue(str);
						}
						if (line.contains("<CityName>")) {
							row.createCell(42).setCellValue(str);
						}
						if (line.contains("<ZipCode>")) {
							row.createCell(43).setCellValue(str);
						}
						if (line.contains("<PhoneNumber>")) {
							row.createCell(44).setCellValue(str);
						}
						if (line.contains("<StreetAddress>")) {
							row.createCell(45).setCellValue(str);
						}
						if (line.contains("<CountryName>")) {
							row.createCell(46).setCellValue(str);
						}
						if (line.contains("<StateName>")) {
							row.createCell(47).setCellValue(str);
						}
						if (line.contains("<StateCode>")) {
							row.createCell(48).setCellValue(str);
						}
					}
				}
				else if (line.contains("<ProgramElement>")) {
					while (!(line = scnr.readLine()).contains("</ProgramElement>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<Code>")) {
							row.createCell(49).setCellValue(str);
						}
						if (line.contains("<Text>")) {
							row.createCell(50).setCellValue(str);
						}
					}
				}
				else if (line.contains("<ProgramReference>")) {
					String code = "";
					String text = "";
					while (!(line = scnr.readLine()).contains("</ProgramReference>")) {
						matcher = pattern.matcher(line);
						while (matcher.find()) {
							str = matcher.group(1);
						}
						if (line.contains("<Code>")) {
							code = str;
						}
						if (line.contains("<Text>")) {
							text = str;
						}
					}
					programReferences.add(new ProgramReference(code, text));
				}
				
				//Fills in the investigators
				for (int i = 0; i < investigators.size() && i < NUM_INVESTIGATORS; i++) {
					row.createCell(16 + i * 6).setCellValue(investigators.get(i).getInvestigatorFirstName());
					row.createCell(17 + i * 6).setCellValue(investigators.get(i).getInvestigatorLastName());
					row.createCell(18 + i * 6).setCellValue(investigators.get(i).getInvestigatorEmailAddress());
					row.createCell(19 + i * 6).setCellValue(investigators.get(i).getInvestigatorStartDate());
					row.createCell(20 + i * 6).setCellValue(investigators.get(i).getInvestigatorEndDate());
					row.createCell(21 + i * 6).setCellValue(investigators.get(i).getInvestigatorRoleCode());
				}
				
				//Fills in the reference codes
				for (int i = 0; i < programReferences.size() && i < NUM_REFERENCES; i++) {
					row.createCell(51 + i * 2).setCellValue(programReferences.get(i).getProgramReferenceCode());
					row.createCell(52 + i * 2).setCellValue(programReferences.get(i).getProgramReferenceText());
				}
				
				//Num investigators
				row.createCell(40).setCellValue(investigators.size());
				
				//Num references
				row.createCell(133).setCellValue(programReferences.size());
			}
			
			//The file is likely corrupted
			if (investigators.size() == 0 && programReferences.size() == 0 && 
					row.getCell(49) == null && row.getCell(41) == null) {
				System.out.println("Error reading: " + file.getName());
				sheet.removeRow(row);
			}
			scnr.close();
		} catch (FileNotFoundException E) {
			System.out.println("No such file found");
		} catch (IOException E) {
			System.out.println("Error reading: " + file.getName());
			sheet.removeRow(row);
		}
		catch (NoSuchElementException E) {
			System.out.println("Error reading: " + file.getName());
			sheet.removeRow(row);
		}
	}
}
