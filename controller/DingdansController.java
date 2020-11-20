package com.yyh.movie.controller;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yyh.movie.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yyh.movie.common.ResourceUtil;
import com.yyh.movie.common.bean.AjaxJson;
import com.yyh.movie.common.bean.BusinessException;
import com.yyh.movie.common.bean.DataGrid;
import com.yyh.movie.common.constants.Globals;
import com.yyh.movie.common.hql.CriteriaQuery;
import com.yyh.movie.common.hql.HqlGenerateUtil;
import com.yyh.movie.common.utils.MyBeanUtils;
import com.yyh.movie.common.utils.TagUtil;
import com.yyh.movie.service.OrderBodyServiceI;
import com.yyh.movie.service.SystemService;

/**   
 * Controller
 * 订单body
 */
@Controller
@RequestMapping("/orderBodyController")
public class DingdansController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(DingdansController.class);

	@Autowired
	private OrderBodyServiceI orderBodyService;
	@Autowired
	private SystemService systemService;
	


	/**
	 * 订单body列表
	 */
	@RequestMapping(params = "list")
	public ModelAndView liebiao(HttpServletRequest request) {
		String orderId = request.getParameter("orderId");
		request.setAttribute("orderId",orderId);
		return new ModelAndView("movie/dingdant");
	}
	/**
	 * 评价
	 */
	@RequestMapping(params = "comment")
	@ResponseBody
	public String neirong(HttpServletRequest request,HttpServletResponse  response){
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user == null) {
			//单点登录 - 返回链接
			String returnURL = (String)request.getSession().getAttribute("ReturnURL");
			if(StringUtils.isNotEmpty(returnURL)){
				request.setAttribute("ReturnURL", returnURL);
			}
			return "login/login";
		}
		
		
		String movieName=  request.getParameter("movieName");
		String comment = request.getParameter("comments");
		String orderId = request.getParameter("orderId");
		comment= comment.replace("；",";");

		String[] split = comment.split(";");

		MovieEntity entity = systemService.<MovieEntity>findHql(" from MovieEntity where movieName=?",movieName).get(0);

		MovieCommEntity commEntity = new MovieCommEntity();
		try {
			commEntity.setContent(comment);
		} catch (Exception e) {
			commEntity.setContent(split[1]);
		}
		
		try {
			commEntity.setScore(Double.parseDouble(split[0]));
		}catch (Exception e){
			commEntity.setScore(Double.parseDouble("5"));
		}
		commEntity.setCreateBy(user.getUserName());
		commEntity.setCreateName(user.getUserName());
		commEntity.setCreateDate(new Date());
		commEntity.setMovieId(entity.getId());
		commEntity.setMovieName(entity.getMovieName());
		commEntity.setOrderId(orderId);
		systemService.save(commEntity);

		return "评价成功";
	}



	/**
	 * easyui datagrid方法
	 */
	@RequestMapping(params = "datagrid")
	public void datagrid(OrderBodyEntity orderBody,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {

		String orderId = request.getParameter("orderId");

		System.out.println(orderId);

		if(StringUtils.isNotEmpty(orderId)){
			orderBody.setOrderId(orderId);
		}
		CriteriaQuery cq = new CriteriaQuery(OrderBodyEntity.class, dataGrid);
		//查询条件组装器
		HqlGenerateUtil.installHql(cq, orderBody, request.getParameterMap());
		cq.add();
		this.orderBodyService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}
	
	/**
	 * 删除订单body
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson shanchu(OrderBodyEntity orderBody, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		orderBody = systemService.getEntity(OrderBodyEntity.class, orderBody.getId());
		message = "订单单身删除成功";
		try{
			orderBodyService.delete(orderBody);
			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单身删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 批量删除订单body
	 */
	@RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson plshanchu(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单身删除成功";
		try{
			for(String id:ids.split(",")){
				OrderBodyEntity orderBody = systemService.getEntity(OrderBodyEntity.class, 
				id
				);
				orderBodyService.delete(orderBody);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单身删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加订单body
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson tianjia(OrderBodyEntity orderBody, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单身添加成功";
		try{
			orderBodyService.save(orderBody);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单身添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 更新订单body
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson genxin(OrderBodyEntity orderBody, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单身更新成功";
		OrderBodyEntity t = orderBodyService.get(OrderBodyEntity.class, orderBody.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(orderBody, t);
			orderBodyService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "订单单身更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

}
