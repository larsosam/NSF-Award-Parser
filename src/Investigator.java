
public class Investigator {
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String startDate;
	private String endDate;
	private String roleCode;
	
	public Investigator() {
		firstName = "";
		lastName = "";
		emailAddress = "";
		startDate = "";
		endDate = "";
		roleCode = "";
		
	}
	
	public Investigator(String firstName, String lastName, String emailAddress, String startDate, String endDate, String roleCode) {
		this.firstName = firstName + "";
		this.lastName = lastName + "";
		this.emailAddress = emailAddress + "";
		this.startDate = startDate + "";
		this.endDate = endDate + "";
		this.roleCode = roleCode + "";
	}
	
	public String getInvestigatorFirstName() {
		return firstName;
	}
	
	public String getInvestigatorLastName() {
		return lastName;
	}
	
	public String getInvestigatorEmailAddress() {
		return emailAddress;
	}
	
	public String getInvestigatorStartDate() {
		return startDate;
	}
	
	public String getInvestigatorEndDate() {
		return endDate;
	}
	
	public String getInvestigatorRoleCode() {
		return roleCode;
	}

	
	//Mutators
	public void setInvestigatorFirstName(String firstName) {
		 this.firstName = firstName;
	}
	
	public void setInvestigatorLastName(String lastName) {
		 this.lastName = lastName;
	}
	
	public void setInvestigatorEmailAddress(String emailAddress) {
		 this.emailAddress = emailAddress;
	}
	
	public void setInvestigatorStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	public void setInvestigatorEndDate(String endDate) {
		 this.endDate = endDate;
	}
	
	public void setInvestigatorRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}
}
