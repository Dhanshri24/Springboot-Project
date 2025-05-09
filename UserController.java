package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryService;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.util.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private CartService cartService;
	
	@Autowired
	private OrderService orderService;
	
	
	@GetMapping("/")
	public String home()
	{
		return "user/home";
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
	
	@GetMapping("/addCart")
	public String addToCart(@RequestParam Integer pid,@RequestParam Integer uid,RedirectAttributes redirectAttributes)
	{
		
	Cart saveCart=cartService.saveCart(pid, uid);
	
	
	
	if(ObjectUtils.isEmpty(saveCart))
	{
		redirectAttributes.addAttribute("errorMsg", "product add to cart fail");
	}
	else
	{
		redirectAttributes.addAttribute("succMsg", "product added to cart");

	}
		
		return"redirect:/product/"+pid;
	}
	
	
	@GetMapping("/cart")
	public String localCartPage(Principal p,Model m)
	{
		
	      UserDtls user=getUserLoggedInDetails(p);
	      
	     List<Cart> carts=cartService.getCartsByUser(user.getId());
	
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
	Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderAmount();
		
		m.addAttribute("totalOrderPrice",totalOrderPrice );
		}
		return "/user/cart";
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid)
	{
	   cartService.updateQuantity(sy,cid);
		 
		return "redirect:/user/cart";
	}

	private UserDtls getUserLoggedInDetails(Principal p) {
		String name=p.getName();
		UserDtls userDtls=userService.getUserByEmail(name);
		return userDtls;
	}
	
	@GetMapping("/orders")
	public String orderPage(Principal p,Model m)
	{
		
		UserDtls user=getUserLoggedInDetails(p);
	      
	     List<Cart> carts=cartService.getCartsByUser(user.getId());
	
		m.addAttribute("carts", carts);
		if(carts.size()>0) {
	Double orderPrice=carts.get(carts.size()-1).getTotalOrderAmount();
	Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderAmount()+250+100;
	
	m.addAttribute("orderPrice",orderPrice );

		m.addAttribute("totalOrderPrice",totalOrderPrice );
		}
		return "/user/order";
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request,Principal p)
	{
		//System.out.println(request);
		UserDtls user=getUserLoggedInDetails(p);
		
		
		orderService.saveOrder(user.getId(), request);
		return "redirect:/user/success";
	}
	
	@GetMapping("/success")
	public String loadSuccess()
	{
		return "/user/success";
	}
	
	@GetMapping("/user-orders")
	public String myOrder(Model m ,Principal p)
	{
		UserDtls loginUser=getUserLoggedInDetails(p);
		List<ProductOrder> orders=orderService.getOrderByUser(loginUser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}
	
	@GetMapping("/update-status")
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
		return "redirect:/user/user-orders";
	}

}
