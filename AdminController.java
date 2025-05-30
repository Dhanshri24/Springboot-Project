package com.ecom.controller;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	@GetMapping("/")
	public String index()

	{
		return "admin/index";
		
	}
	@GetMapping("/loadAddProduct")
	public String loadAddProduct(Model m)

	{
		
		List<Category> categories=categoryService.getAllCategory();
		m.addAttribute("categories", categories);
		return "admin/add_product";
		
	}
	@GetMapping("/category")
	public String category(Model m)

	{
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/category";
		
	}
	
	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category,
	                            @RequestParam("file") MultipartFile file, 
	                            RedirectAttributes redirectAttributes) throws IOException {

		 
		   
		   
		
	    String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
	    
	    
	    category.setImageName(imageName);
	    

	    Boolean existCategory = categoryService.existCategory(category.getName());

	    if (existCategory) {
	        redirectAttributes.addFlashAttribute("errorMsg", "Category Name Already Exists");
	    } else {
	        Category saveCategory = categoryService.saveCategory(category);

	        if (ObjectUtils.isEmpty(saveCategory)) {
	            redirectAttributes.addFlashAttribute("errorMsg", "Not Saved! Internal Server Error");
	        } else {
	        	
	            File saveFile=new ClassPathResource("static/img").getFile();
	        	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
	        	System.out.println(path);
	        	Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
	        	
	            redirectAttributes.addFlashAttribute("succMsg", "Saved Successfully");
	        }
	    }

	    // Redirect to the category page
	    return "redirect:/admin/category";
	}
	
	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, RedirectAttributes redirectAttributes)
	{
		
		Boolean deleteCategory=categoryService.deleteCategory(id);
		
		if(deleteCategory)
		{
			redirectAttributes.addFlashAttribute("succMsg", " Category Deleted Successfully");
			
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMsg", "Error");
		}
		return "redirect:/admin/category";
		
		
	}
	
	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id ,Model m)

	{
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
		
	}
	
	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {

	    Category oldcategory = categoryService.getCategoryById(category.getId());

	    // Make sure to handle if category object is not null and update the fields
	    if (category != null) {
	        oldcategory.setName(category.getName());
	        oldcategory.setIsActive(category.getIsActive());
	        String imageName = file.isEmpty() ? oldcategory.getImageName() : file.getOriginalFilename();
	        oldcategory.setImageName(imageName);
	    }

	    // Save the updated category
	    Category updatedCategory = categoryService.saveCategory(oldcategory);

	    if (updatedCategory != null) {
	        // Handle file upload
	        if (!file.isEmpty()) {
	            File saveFile = new ClassPathResource("static/img").getFile();
	            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator + file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	        }

	        redirectAttributes.addFlashAttribute("succMsg", "Category Updated Successfully");
	    } else {
	        redirectAttributes.addFlashAttribute("errorMsg", "Error");
	    }

	    return "redirect:/admin/loadEditCategory/" + category.getId();
	}
	
	@PostMapping("/saveProduct")
	public String saveProduct(@ModelAttribute Product product,  @RequestParam("file") MultipartFile image,RedirectAttributes redirectAttributes) throws IOException
	{
		String imageName=image.isEmpty()?"default.jpg"	:image.getOriginalFilename();	
		product.setImage(imageName);
		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());
		
		Product saveProduct=productService.saveProduct(product);
		if(!ObjectUtils.isEmpty(saveProduct))
		{
			
			File saveFile=new ClassPathResource("static/img").getFile();
        	Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());
        	//System.out.println(path);
        	Files.copy(image.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
               redirectAttributes.addFlashAttribute("succMsg", " Product Added Successfully");
			
		}
		else
		{
			redirectAttributes.addFlashAttribute("errorMsg", "Error");
		}
		
		return"redirect:/admin/loadAddProduct";
	}
	
	@GetMapping("/products")
	public String loadViewProduct(Model m)
	{
		m.addAttribute("products",productService.getAllProducts());
		return "admin/products";
	}
	
	@GetMapping("/deleteProduct/{id}")
	public String deleteProduct(@PathVariable int id,RedirectAttributes redirectAttributes) 
	
	{
		
		Boolean deleteProduct=productService.deleteProduct(id);
		if(deleteProduct)
		{
			
				redirectAttributes.addFlashAttribute("succMsg", " Product Deleted Successfully");
				
			}
			else
			{
				redirectAttributes.addFlashAttribute("errorMsg", "Error");
			}
			
		
		return "redirect:/admin/products";
	}
	
	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m)
	{
		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/edit_product";
	}
	
	@PostMapping("/updateProduct")
	public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
			RedirectAttributes redirectAttributes, Model m) {

		
		if(product.getDiscount() < 0 || product.getDiscount() > 100)
			
		{
			redirectAttributes.addFlashAttribute("errorMsg", "Invalid Discount");
			
		}
		else
		{
		
		Product updateProduct = productService.updateProduct(product, image);
		if (!ObjectUtils.isEmpty(updateProduct)) {
			
			redirectAttributes.addFlashAttribute("succMsg", " Product Deleted Successfully");
			
			
		} else {
			
			redirectAttributes.addFlashAttribute("errorMsg", "Error");
			
		}
		}

		return "redirect:/admin/editProduct/" + product.getId();
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
	
	
	@GetMapping("/users")
	public String getAllUsers(Model m)
	{
		
		List<UserDtls> users=userService.getUser("ROLE_USER");
		m.addAttribute("users", users);
		return "/admin/users";
	}
	
	
	@GetMapping("/updateSts")
	public String updateUserAccountStatus(@RequestParam Boolean status,@RequestParam Integer id,RedirectAttributes redirectAttributes,Model m)
	{
		Boolean f=userService.updateAccountStatus(id,status);
		if(f)
		{
			redirectAttributes.addAttribute("succMsg", "Account Status Updated");
			
		}
		else
		{
			redirectAttributes.addAttribute("errorMsg", "Update Failed");

		}
		return "redirect:/admin/users";
	}
	
	@GetMapping("/orders")
	public String getAllOrders(Model m)
	{
		List<ProductOrder> allOrders=orderService.getAllOrders();
		m.addAttribute("orders", allOrders);
		
		return "/admin/orders";
	}
	
	@PostMapping("/update-order-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st, HttpSession session) {

		OrderStatus[] values = OrderStatus.values();
		String status = null;

		for (OrderStatus orderSt : values) {
			if (orderSt.getId().equals(st)) {
				status = orderSt.getName();
			}
		}

		Boolean updateOrder = orderService.updateOrderStatus(id, status);

		if (updateOrder) {
			session.setAttribute("succMsg", "Status Updated");
		} else {
			session.setAttribute("errorMsg", "status not updated");
		}
		return "redirect:/admin/orders";
	}
	

	
	
	
}
