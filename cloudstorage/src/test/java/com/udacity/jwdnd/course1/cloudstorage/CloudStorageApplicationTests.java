package com.udacity.jwdnd.course1.cloudstorage;

import com.udacity.jwdnd.course1.cloudstorage.mapper.NoteMapper;
import com.udacity.jwdnd.course1.cloudstorage.page.*;
import com.udacity.jwdnd.course1.cloudstorage.common.Message;

import com.udacity.jwdnd.course1.cloudstorage.result.Credential;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CloudStorageApplicationTests {

	@LocalServerPort
	private int port;

	private WebDriver driver;

	private String localhostPath = "http://localhost:";
	private final String signupPath = "/signup";
	private final String loginPath = "/login";
	private final String homePath = "/home";
	private final String filesPath = "/user/files/list";
	private final String notesPath = "/user/notes/list";
	private final String credentialsPath = "/user/credentials/list";

	// For Signup and Login
	private String firstName = "user1FirstName";
	private String lastName = "user1LastName";
	private String username = "user1";
	private String password = "user1pwd";

	// For Files
	private String filePath = "C:\\FileTestPath\\";
	private String fileName = "testfile1.txt";

	// For Note Dialog
	private String noteTitle = "newNote1";
	private String noteDescription = "This is the first created note.";
	private String newDescription = "This is a new description";

	// For Credentials Dialog
	private String credentialUrl1 = "http://email.com";
	private String credentialUsername1 = "cuser1";
	private String credentialPassword1 = "cpassword1";
	private String credentialUsername2 = "cuser2";
	private String credentialPassword2 = "cpassword2";

	private Message Messages = new Message();

	@BeforeAll
	static void beforeAll() {
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	public void beforeEach() throws InterruptedException {
		this.driver = new ChromeDriver();

		testSignupLoginLogout();
	}

	@AfterEach
	public void afterEach() {
		if (this.driver != null) {
			driver.quit();
		}
	}

	public void testSignupLoginLogout() throws InterruptedException {
		String localContextPath = localhostPath + this.port + "/superduperdrive";
		// Signup Page
		driver.get(localContextPath + signupPath);
		SignupPage signupPageObject = new SignupPage(driver);

		assertEquals("Sign Up", driver.getTitle());

		signupPageObject.performSignup(firstName, lastName, username, password);

		assertEquals(true, signupPageObject.isSignupSuccessful());

		// Login Page
		login();

		HomePage homePageObject = new HomePage(driver);

		// Logout
		homePageObject.performLogout();

		assertEquals("Login", driver.getTitle());

		// Check Home Page
		driver.get(localContextPath + homePath);
		assertNotEquals("Home", driver.getTitle());
	}

	@Test
	public void getLoginPage() throws InterruptedException {
		String contextPath = this.port + "/superduperdrive";
		driver.get(localhostPath +  contextPath + loginPath);
		assertEquals("Login", driver.getTitle());
		Thread.sleep(3000);
	}

	@Test
	public void testAuthorization() {
		String localContextPath = localhostPath + this.port + "/superduperdrive";

		driver.get(localContextPath + signupPath);
		assertEquals("Sign Up", driver.getTitle());

		driver.get(localContextPath + loginPath);
		assertEquals("Login", driver.getTitle());

		driver.get(localContextPath + homePath);
		assertNotEquals("Home", driver.getTitle());

		driver.get(localContextPath + filesPath);
		assertNotEquals("Home", driver.getTitle());

		driver.get(localContextPath + notesPath);
		assertNotEquals("Home", driver.getTitle());

		driver.get(localContextPath + credentialsPath);
		assertNotEquals("Home", driver.getTitle());
	}

	@Test
	public void testFileOperations() throws InterruptedException {
		login();

		FilesTab filesTabObject = new FilesTab(driver);

		System.out.println(filePath + fileName);

		// File Upload
		filesTabObject.performUpload(filePath + fileName);

		Thread.sleep(3000);

		assertEquals(true, filesTabObject.isFileDisplayed(fileName));

		// File Delete
		int id = filesTabObject.performDelete(1);

		assertEquals(false, filesTabObject.isFileDisplayedById(id));
	}

	@Test
	public void testNotesOperations() throws Exception {
		login();

		NotesTab notesTabObject = new NotesTab(driver);
		Thread.sleep(5000);

		notesTabObject.clickNotesTab();

		Thread.sleep(5000);

		// Add new Note
		assertEquals(true, notesTabObject.clickAddNewNote());

		Thread.sleep(5000);

		notesTabObject.performAdd(noteTitle, noteDescription);

		Thread.sleep(5000);

		assertEquals(true, notesTabObject.isTitleDisplayed(noteTitle));

		// Edit note
		assertEquals(true, notesTabObject.performEdit(Integer.toString(1), noteTitle, "This is a new description"));
		assertEquals(newDescription, notesTabObject.getDescription(noteTitle));

		Thread.sleep(5000);

		// Delete note
		int deletedNoteId = notesTabObject.performDelete(Integer.toString(1));

		Thread.sleep(5000);

		assertEquals(false, notesTabObject.isNoteDisplayed(deletedNoteId));
	}

	@Test
	public void testCredentialOperations() throws Exception {
		login();

		Thread.sleep(3000);

		CredentialsTab credentialsTabObject = new CredentialsTab(driver);

		credentialsTabObject.clickCredentialsTab();

		Thread.sleep(3000);

		addCredential(credentialsTabObject);

		Thread.sleep(3000);

		editCredential(credentialsTabObject);;

		Thread.sleep(3000);

		deleteCredential(credentialsTabObject);

		Thread.sleep(3000);
	}

	private void editCredential(CredentialsTab credentialsTabObject) throws InterruptedException {
		String credentialId = credentialsTabObject.performEdit(Integer.toString(1), credentialUsername2, credentialPassword2);

		Credential resultCredential = credentialsTabObject.getCredentialById(Integer.parseInt(credentialId));

		assertEquals(true, resultCredential != null && resultCredential.getUsername().equals(credentialUsername2) && resultCredential.getPassword().equals(credentialPassword2));
	}

	private void addCredential(CredentialsTab credentialsTabObject) throws InterruptedException {
		assertEquals(true, credentialsTabObject.clickAddNewCredential());

		Thread.sleep(3000);

		credentialsTabObject.performAddNewCredential(credentialUrl1, credentialUsername1, credentialPassword1);

		Credential resultCredential = credentialsTabObject.getCredentialByUrl(credentialUrl1);

		assertEquals(true, resultCredential != null && resultCredential.getUsername().equals(credentialUsername1) && resultCredential.getPassword().equals(credentialPassword1));
	}

	private void deleteCredential(CredentialsTab credentialsTabObject) throws Exception {
		int deletedCredentialId = credentialsTabObject.performDelete(Integer.toString(1));

		assertEquals(false, credentialsTabObject.doesCredentialExists(deletedCredentialId));
	}

	private void login() throws InterruptedException {
		String localContextPath = localhostPath + this.port + "/superduperdrive";
		// Login Page
		getLoginPage();

		LoginPage loginPageObject = new LoginPage(driver);
		loginPageObject.performLogin(username, password);

		// Check Home Page
		assertEquals(localContextPath + filesPath, driver.getCurrentUrl());
		assertEquals("Home", driver.getTitle());
	}
}