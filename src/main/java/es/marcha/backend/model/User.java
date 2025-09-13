package es.marcha.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {
	//Attribs
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(name="name")
	private String name;
	@Column(name="username")
	private String username;
	@Column(name="password")
	private String password;
	@Column(name="email")
	private String email;
	@Column(name="phone")
	private String phone;
	@Column(name="status")
	private boolean status;
	@Column(name="email_verified_at")
	private LocalDateTime email_verified_at;
	private boolean locked;
	@Column(name="last_login_at")
	private LocalDateTime last_login_at;
	@Column(name="created_at")
	private LocalDateTime created_at;
	@OneToOne(mappedBy = "user")
	private Client client;
	@OneToOne
	@JoinColumn(name="role_id", referencedColumnName = "id")
	private Role role;
	
	//Getters and Setters
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public LocalDateTime getEmail_verified_at() {
		return email_verified_at;
	}
	public void setEmail_verified_at(LocalDateTime email_verified_at) {
		this.email_verified_at = email_verified_at;
	}
	public boolean isLocked() {
		return locked;
	}
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	public LocalDateTime getLast_login_at() {
		return last_login_at;
	}
	public void setLast_login_at(LocalDateTime last_login_at) {
		this.last_login_at = last_login_at;
	}
	public LocalDateTime getCreated_at() {
		return created_at;
	}
	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}
	public Role getRole_id() {
		return role;
	}
	public void setRole_id(Role role) {
		this.role = role;
	}
	
	
	
	

}
