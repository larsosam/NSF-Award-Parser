
public class ProgramReference {
	private String code;
	private String text;
	
	public ProgramReference(String code, String text) {
		this.code = code + "";
		this.text = text + "";
	}
	
	public String getProgramReferenceCode() {
		return code;
	}
	
	public String getProgramReferenceText() {
		return text;
	}
}
