package com.ecom.controller;





import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.CommanUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;


@Controller
public class HomeController {

	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private CommanUtil commanUtil;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	
	@GetMapping("/")
	public String index()
{
	return "index";
		
}
	
	@GetMapping("/signin")
	public String login()
{
	return "login";
		
}
	
	@GetMapping("/register")
	public String register()
{
	return "register";
		
}
	
	
	@GetMapping("/products")
	public String products(Model m,@RequestParam(value="category",defaultValue = "") String category) {
		// System.out.println("category="+category);
		List<Category> categories = categoryService.getAllActiveCategory();
		List<Product> products = productService.getAllActiveProducts(category);
		m.addAttribute("categories", categories);
		m.addAttribute("products", products);
		m.addAttribute("paramValue", category);
		return "product";
	}
	
	
	
	@GetMapping("/product/{id}")
	public String product(@PathVariable int id,Model m)
{
		Product productById=productService.getProductById(id);
		m.addAttribute("product", productById);
		
	return "view_product";
		
}
	@PostMapping("/saveUser")
	public String saveUser(@ModelAttribute UserDtls user,@RequestParam("img") MultipartFile file,RedirectAttributes redirectAttributes) throws IOException
	{
		
		String imageName=file.isEmpty()?"default.jpg":file.getOriginalFilename();
		
		user.setProfileImage(imageName);
		
		UserDtls saveUser=userService.saveUser(user);
		
		if(!ObjectUtils.isEmpty(user))
		{
			
			File saveFile=new ClassPathResource("static/img").getFile();
        	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
        	//System.out.println(path);
        	Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
             
			
		
		 redirectAttributes.addFlashAttribute("succMsg", " Register Successfully");
	}
	else
	
	{
		redirectAttributes.addFlashAttribute("errorMsg", "Error");
	}
		
		return "redirect:/register";
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m)
	{
		if(p!=null)
		{
			String email=p.getName();
			UserDtls userDtls=userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart=cartService.getCountCart(userDtls.getId());
			m.addAttribute("cartCount", countCart);
		}
		
		List<Category> allActiveCategory=categoryService.getAllActiveCategory();
		m.addAttribute("category", allActiveCategory);
	}
	
	//forget password
	
	@GetMapping("/forgot-password")
	public String showForgetPassword()
	{
		return"forgot_password.html";
	}
	
	@PostMapping("/forgot-password")
	public String processForgetPassword(@RequestParam String email,RedirectAttributes redirectAttributes,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException
	{
		UserDtls userByEmail=userService.getUserByEmail(email);
		
		if(ObjectUtils.isEmpty(userByEmail))
		{
			redirectAttributes.addAttribute("errorMsg", "Invalid Email");
		}else
		{
			String resetToken=UUID.randomUUID().toString();
			
			userService.updateUserResetToken(email,resetToken);
			
			
			String url=CommanUtil.generateUrl(request)+"/reset-password?token="+resetToken;
			
			Boolean sendMail=commanUtil.sendMail(url,email);
			
			if(sendMail)
			{
				redirectAttributes.addAttribute("succMsg", "please check your email...Password reset link sent");

			}
			else
			{
				redirectAttributes.addAttribute("errorMsg", "somethinf wrong in server Mail not send");

			}
			
			
			
		}
		
		return"redirect:/forgot-password";
	}
	
	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token,RedirectAttributes redirectAttributes,Model m)
	{
		UserDtls userByToken=userService.getUserByToken(token);
		
		if(userByToken==null)
		{
			redirectAttributes.addAttribute("msg", "Invalid or Expired Link");
			return "message";
		}
		m.addAttribute("token", token);
		
		return"reset_password";
	}
	
	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token,@RequestParam String password, RedirectAttributes redirectAttributes,Model m)
	{
		UserDtls userByToken=userService.getUserByToken(token);
		
		if(userByToken==null)
		{
			redirectAttributes.addAttribute("msg", "Invalid or Expired Link");
			return "message";
		}else
		{
			userByToken.setPassword(passwordEncoder.encode(password));
			userByToken.setReset_token(null);
			userService.updateUser(userByToken); 
			redirectAttributes.addFlashAttribute("succMsg", "Password Change Sucessfully");
			m.addAttribute("msg", "Password Change Sucessfully");
			return"message"; 

		}
		
		
	}
	
	
	
	
	

}