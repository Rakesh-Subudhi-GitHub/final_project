package com.smart.control;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.rk.helper.Message;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepo;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.MyOrder;
import com.smart.entity.User;

import com.razorpay.*;


@Controller
@RequestMapping("/user")
public class UserController {

	//user parent repository
	@Autowired
	private UserRepository userRepo;
	
	//contact child repository
	@Autowired
	private ContactRepository contRepo;
	
	
	//only for password so convert BCryptPasswordEncoder to normal password thats way 
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	
	@Autowired
	private MyOrderRepo myOrderRepo;
	
	//payment module connect
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> map,Principal principal) throws Exception
	{
		System.out.println("Payment is loaded...");
		System.out.println(map);
		
		Integer amt = Integer.parseInt(map.get("TotalAmount").toString());
		
		//import razor pay class and implements 

			var client=new RazorpayClient("rzp_test_WtE0pWsJK15cKR", "A9D76PK03MnaZ2V7apnWVQO0");
			
			JSONObject ob=new JSONObject();
			ob.put("amount", amt*100);//amount is allows in (paise) not accept in ruppes
			ob.put("currency", "INR");
			ob.put("receipt", "txn_21457");
			
			//Creating order this ob send to razor server
			Order order=client.Orders.create(ob);
			
			System.out.println("Order sent details : "+order);
			
		/* You can save the amount transaction then save DataBase for better convanean*/	
			
	////////////////////////////////////////////////////////////////////////////////////////
			//set the table value
			MyOrder myOrder = new MyOrder();
			
			myOrder.setAmount(order.get("amount")+"");//amount is integer but it take string so converting
			myOrder.setOrderId(order.get("id"));
			myOrder.setPaymetId(null);//if is success full then save it so fast set null;
			myOrder.setStatus("created");
			myOrder.setReceipt(order.get("receipt"));
			
			//set user name
			User username=userRepo.getUserByUserName(principal.getName());
			System.out.println("user details :: "+username);
			myOrder.setUser(username);
			
			//save the data
			myOrderRepo.save(myOrder);
			
		/////////////////////////////////////////////////////////////////////////////////////////////	
			
			return order.toString();//return the map<> of order
		
	}//payment method
	
	
	//Update the Traction table
	
	@PostMapping("/update_order")
	public ResponseEntity<?> UpdateOrder(@RequestBody Map<String, Object> mapData)
	{
		System.out.println("Update Order details"+mapData);
		
		
		//take all data of poticular user take all details by help of (Order_Id)
		MyOrder myOrder = myOrderRepo.findByOrderId(mapData.get("order_id").toString());
		
		myOrder.setPaymetId(mapData.get("payment_id").toString());//update this bcz previous it take Null so change it
		
		myOrder.setStatus(mapData.get("status").toString());
		
		//then finally update
		myOrderRepo.save(myOrder);
		
		return ResponseEntity.ok(Map.of("msg","Successfully done"));
	}
	
	
	
	//in this class all method having pass user name so 
				//create a method and return name for all
	//common data
	@ModelAttribute //its run automatically when run this class
	public void addCommondata(Model m,Principal principal)
	{
		//user email in help of principal
		String UserName = principal.getName();
				
		//print the username(mail)
		System.out.println("UserName is :: "+UserName);//this is the main key of the each user
				
		//use UserRepository and collect all data of (User table) like name,id,mail,about,password,image,role
		User user = userRepo.getUserByUserName(UserName);
		System.out.println("user is :: "+user);
				
		//print all user data in web page transfer userdata
		m.addAttribute("userdata",user);
		
		//user name is
		System.out.println("user name is :: "+user.getName());
	}
	
	
	
	
	
	//home-dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		//user email in help of principal
		String UserName = principal.getName();
						
		//print the username(mail)
		System.out.println("UserName is :: "+UserName);//this is the main key of the each user
						
		//use UserRepository and collect all data of (User table) like name,id,mail,about,password,image,role
		User user = userRepo.getUserByUserName(UserName);
		System.out.println("user is :: "+user);
						
		//print all user data in web page transfer userdata
		model.addAttribute("userdata",user);
				
		model.addAttribute("title",user.getName()+"-Home");
		
		
		
		return "User/user_dashboard";   //re-direct file to User-->user_dashboard
	
	
	}//method
	
	
	
	
	
	
	
	
	//addcontact handler
	@GetMapping("/addcontact")
	public String AddContact(Model model)
	{
		model.addAttribute("title","AddContact");
		model.addAttribute("contact",new Contact());
		
		return "User/addContact";
	}//method
	
	
	//add contact
	@PostMapping("/addContactDetails")
	public String insertContact(@ModelAttribute Contact contact, //Contact.class matching with addContact.html file carry with that name="" value
								    @RequestParam("profileimage") MultipartFile file, //image file save useing help of (MultipartFile)
								    				//Normally in addcontact.html image store name="profileimage" set it 
											Principal principal,//user details collect
												HttpSession session,//Success msg
													Model model)//model data
	{
		model.addAttribute("title","AddContact");
		
	try {
		
		//user mail
		String mail=principal.getName();
		System.out.println("User mail id is :: "+mail);
		
		//user hole details
		User user=userRepo.getUserByUserName(mail);
		
/*==========================================================================================================*/		
		
		//processing and uploading file...
		
		if(file.isEmpty())
		{
			//if file is empty then message
			System.out.println("image is empty:: ");
			
			//add default image
			contact.setImage("contact.png");
			
		}
		else
		{
			//upload the file to folder and upfdate the name to content
			contact.setImage(file.getOriginalFilename());//set the file name 
			
			File savefile = new ClassPathResource("static/image").getFile();
			
			
			//path of store file name or store  
			Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), //source file location
								path ,//Destination file location
								  StandardCopyOption.REPLACE_EXISTING );//store file thats way
		System.out.println("image is uploaded");
		
		}//else
		
		
		
		
/*---------------------------------------------------------------------------------------------------------*/

		//add child fast
		contact.setUser(user);
		
		/* fast load child  them collect the contact and add the poticular contact (list)*/
		
		//add to parent to child
		user.getContact().add(contact);
		
		//save the obj help of parent 
		userRepo.save(user);
		
		
		System.out.println("Added to data base");
		
		//check the contact
		System.out.println("contact data :: "+contact);
		
		//success all are added....
		session.setAttribute("message",new Message("Your contact is added !! add more..","success"));//helper class com.smart.helper(Message.class)
		
		//any error check then
			//throw new Exception("pass the message");
		
	}//try
	
	catch (Exception e) {
	
		System.out.println("Error msg is "+e.getMessage());
		e.printStackTrace();
		
		//else problem...
		session.setAttribute("message",new Message("Your contact is not added !! some internal problem..","danger"));//helper class com.smart.helper(Message.class)
		
	}
	
	return "User/addContact";
	
	}//method
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//show contacts handler 
	
	//per page=5 contact 5[n]
	//current page=0[page]
	@GetMapping("/viewContact/{page}") //thats way use current page so need {page}
	public String showContacts(@PathVariable("page") Integer page, //current page check
															Model model,
																Principal principal)
	{
		model.addAttribute("title","View Contacts");

/*====================================  1st way   =========================================================*/		
	
		//fast collect the user mail help of principal
//		String email=principal.getName();
		
		//fetch user details collect 
//		User user=userRepo.getUserByUserName(email);
																	//its not good approaches
		//all contact get
//		List<Contact> contact = user.getContact();
		
//		System.out.println("Get all contact is :::: ");
//		contact.forEach(System.out::println);
		
		//transfer this data 
//		model.addAttribute("contact",contact);
		
/*===========================================================================================================*/		
	
		//find email help of principal
		String email = principal.getName();
		
		//fetch all details in User
		User user=userRepo.getUserByUserName(email);
		
		//pagination                          5-change it the page elements show  per page
		Pageable pageable=PageRequest.of(page,5);//Pagable take 2 obj 1st= "current page" or 2nd= "page size"
		
		Page<Contact> contact = contRepo.findContactByUser(user.getId(),pageable);
		
		//contacts take
		model.addAttribute("contact",contact);
		
		//page takes
		model.addAttribute("currentPage",page);
		
		//total page
		model.addAttribute("totalPage",contact.getTotalPages());//page is send total page automatically
		
		
		return "User/showcontacts";
		
	}//method

//---------------------------------------------------------------------------------------------------------------
	
//handle the particular contact details show
	@GetMapping("/contact/{cid}")
	public String fetchContactDetails(@PathVariable("cid") Integer cid,Model model,Principal principal)
	{
		
		model.addAttribute("title","Contact Details");
		
		System.out.println("Cid provied user is :: "+cid);
		
		//show the contact in id but "optional[contact[]]"
		Optional<Contact> contactOptional = contRepo.findById(cid);//Particular contact find so use Optional<> (or) u can use HQLQuery
		
		//contact particular one array to show "contact[]"
		Contact contact=contactOptional.get();
		
		
		System.out.println("==================================================");
		System.out.println(contactOptional);
		System.out.println("====================================================");
		System.out.println(contact);
		
		//security bug check and fixed
		String email = principal.getName();
		
		//get user
		User user = userRepo.getUserByUserName(email);
		
		//security only access the user that contact only other contact doesnot access
		if(user.getId()==contact.getUser().getId())
		{
		
			//Transfer the contact
			model.addAttribute("contact",contact);
		}
		
		model.addAttribute("title",contact.getName()+" Contact Details");
		
		
		return "User/eachContactDetails";
	
	}//method

//=============================================================================================================
	
	//delete a Contact handler
	@GetMapping("/deleteContact/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal,
																					HttpSession session)
	{
		//optional contact
		Optional<Contact> optionalContact = contRepo.findById(cid);
		
		//each contact
		Contact contact = optionalContact.get();
		
		//check the security
		
		//security bug check and fixed
		String email = principal.getName();
				
		//get user
	    User user = userRepo.getUserByUserName(email);
		
	    if(user.getId()==contact.getUser().getId())
		{
	    	
		//delete contact
		contRepo.delete(contact);
		
		//remove image also
		
		//delete message help of com.rk.helper class
		session.setAttribute("message",new Message("Sucessfully deleted..","success"));
		
		}
		
		return "redirect:/user/viewContact/0";
	}//method
	

	//open update form handler 
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model model)
	{
		Contact contact=contRepo.findById(cid).get();
		
		//use security later
		
		//contact transfer
		model.addAttribute("ContactDetails",contact);
		
		//contact user name pass
		model.addAttribute("name",contact.getName());
		model.addAttribute("image",contact.getImage());
		
		//title pass
		model.addAttribute("title",contact.getName()+"-Update");
		
		
		return "User/update_form";
		
	}//method
	
	
	//update the Contact
	@PostMapping("/change-contactDetails")  //this is same as save the contact ("/addContact") all process same but minner change it 
	public String updateContact(@ModelAttribute Contact contact,//Particular contact that you click that come here
												   @RequestParam("profileimage") MultipartFile file,
												       Model model,HttpSession session,
												                  Principal principal)
			
	{
		
	try {
		
		//fast load contact
		 Contact oldContactDetails = contRepo.findById(contact.getCid()).get();//Fast load Optional then .get() method load particular contact load
		
		//image check if it is change or not	
		if(!file.isEmpty())
		{
			
			//fast delete the previous photo
			File deletefile = new ClassPathResource("static/image").getFile();
			
			File file1=new File(deletefile,oldContactDetails.getImage());//fast take the file in oldfile and load in file1
			
			//then simple delete the file
			file1.delete();
			
			
			//update new photo
			  //add new file
			File savefile = new ClassPathResource("static/image").getFile();
			
			
			//path of store file name or store  
			Path path = Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), //source file location
								path ,//Destination file location
								  StandardCopyOption.REPLACE_EXISTING );//store file thats way
			//save the file
			contact.setImage(file.getOriginalFilename());
		
			System.out.println("=================  save the file =================");
			
		}//if
		
		else {
			
			//that means user does not set new image so as it print image
			contact.setImage(oldContactDetails.getImage());
		}
		
		
	////////////////////// Its imp for update or delete or save contact  /////////////////////////////////////////////////////////////////////	
		
		//load parent(User.class)
		User user=userRepo.getUserByUserName(principal.getName());
		
		//update parent class(User class)
		contact.setUser(user);
		
		//normal save the file //child class save (Contact) 
		contRepo.save(contact);
		
		System.out.println("save ethe file completly:::");
		
	//show message 
	session.setAttribute("message", new Message("Upadte the contact sucessfully ","success"));//help of com.rk.help
		
	}//try
	
	catch (Exception e) {
		
		e.printStackTrace();
		}
	
		System.out.println("Contact of poticular user is :: "+contact);
		System.out.println("Contact id"+contact.getCid());
		
		
		return "redirect:/user/contact/"+contact.getCid();
	
	}//method
	
	
	
	
	
	
	
	//your profile handler
	@GetMapping("/profile")	
	public String yourProfile(Model model,Principal principal)
	{
		
	//user email in help of principal
	String UserName = principal.getName();
											
	User user = userRepo.getUserByUserName(UserName);
						
	model.addAttribute("title",user.getName()+"-Profile");
		
	return"User/profilePage";
		
	}//method

	
///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//open setting handler
	@GetMapping("/setting")
	public String openSetting(Model model,Principal principal)
	{
		User user=userRepo.getUserByUserName(principal.getName());
		
		model.addAttribute("title",user.getName()+"-Setting");
		return "User/setting";
	}//method
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPsw") String oldPsw, @RequestParam("newPsw") String newPsw,
											Principal principal,
											      HttpSession session)
	{
	
		System.out.println("old password ::::"+oldPsw);
		System.out.println("new password ::: "+newPsw);
		
		User user=userRepo.getUserByUserName(principal.getName());
		
		System.out.println("user password is ::::  "+user.getPassword());
	
		//user password is match in oldpassword check
		if(bCryptPasswordEncoder.matches(oldPsw, user.getPassword()))
		{
			//change the password system 
			
			//encrypt the password and set the password
			user.setPassword(bCryptPasswordEncoder.encode(newPsw));
			
			//save the new password
			userRepo.save(user);
			
			//show the message
			session.setAttribute("message", new Message("Your password is changed Successfully ... !!!", "success"));
		
		}
		
		else
		{
		
		//error
		//show the message
		session.setAttribute("message", new Message("Please Enter correct old password try again ...", "danger"));
		
		
		//error then going to same page
		
		return "redirect:/user/setting";
		
		}
		
		
		
	return "redirect:/user/index";
	
	}//method

	
	
}//class
