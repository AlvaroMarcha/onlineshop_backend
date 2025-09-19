package es.marcha.backend.dto.request;

public class RequestClientDTO {
	  private String firstName;
	    private String lastName;
	    private String company;
	    private String jobTitle;
	    private String email;
	    private String phone;
	    private String address;

	    private RequestUserDTO user; 

	    // Getters y Setters
	    public String getFirstName() {
	        return firstName;
	    }
	    public void setFirstName(String firstName) {
	        this.firstName = firstName;
	    }
	    public String getLastName() {
	        return lastName;
	    }
	    public void setLastName(String lastName) {
	        this.lastName = lastName;
	    }
	    public String getCompany() {
	        return company;
	    }
	    public void setCompany(String company) {
	        this.company = company;
	    }
	    public String getJobTitle() {
	        return jobTitle;
	    }
	    public void setJobTitle(String jobTitle) {
	        this.jobTitle = jobTitle;
	    }
	    public String getEmail() {
	        return email;
	    }
	    public void setEmail(String email) {
	        this.email = email;
	    }
	    public String getPhone() {
	        return phone;
	    }
	    public void setPhone(String phone) {
	        this.phone = phone;
	    }
	    public String getAddress() {
	        return address;
	    }
	    public void setAddress(String address) {
	        this.address = address;
	    }
	    public RequestUserDTO getUser() {
	        return user;
	    }
	    public void setUser(RequestUserDTO user) {
	        this.user = user;
	    }
}
