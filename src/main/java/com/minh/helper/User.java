package com.minh.helper;

public class User {
	public static UserData JOB_TEST = new UserData("auto_jobtest@viaplay.com", "JobTest1234", "auto", "jobtest");
	
	public static class UserData {
		private String username = null;
		private String password = null;
		private String firstName = null;
		private String lastName = null;
		
		public UserData(String username, String password, String firstName, String lastName) {
			this.username = username;
			this.password = password;
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		public String getUsername() {
			return username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public String getFirstName() {
			return firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
	}
}
