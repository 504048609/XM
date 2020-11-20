package com.yyh.movie.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yyh.movie.common.ResourceUtil;
import com.yyh.movie.common.utils.ContextHolderUtils;
import com.yyh.movie.entity.TSUser;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yyh.movie.common.bean.AjaxJson;
import com.yyh.movie.common.bean.BusinessException;
import com.yyh.movie.common.bean.DataGrid;
import com.yyh.movie.common.constants.Globals;
import com.yyh.movie.common.hql.CriteriaQuery;
import com.yyh.movie.common.hql.HqlGenerateUtil;
import com.yyh.movie.common.utils.MyBeanUtils;
import com.yyh.movie.common.utils.TagUtil;
import com.yyh.movie.entity.OrderHeadEntity;
import com.yyh.movie.service.OrderHeadServiceI;
import com.yyh.movie.service.SystemService;

import java.util.List;

/**   
 *Controller
 *订单head
 */
@Controller
@RequestMapping("/orderHeadController")
public class DingdantController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(DingdantController.class);

	@Autowired
	private OrderHeadServiceI orderHeadService;
	@Autowired
	private SystemService systemService;
	


	/**
	 * 订单head列表
	 */
	@RequestMapping(params = "list")
	public ModelAndView liebiao(HttpServletRequest request) {
		return new ModelAndView("movie/dingdans");
	}

	/**
	 * datagrid方法
	 */
	@RequestMapping(params = "datagrid")
	public void datagrid(OrderHeadEntity orderHead,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		HttpSession session = ContextHolderUtils.getSession();
		TSUser tsUser=(TSUser) session.getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if(!tsUser.getUserName().equals("admin")){
			orderHead.setBuyerName(tsUser.getUserName());
		}
		CriteriaQuery cq = new CriteriaQuery(OrderHeadEntity.class, dataGrid);
		//查询条件组装器
		HqlGenerateUtil.installHql(cq, orderHead, request.getParameterMap());
		//cq.add(Restrictions.eq("createBy","guke"));
		cq.add();
		this.orderHeadService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}
	

	/**
	 *操作订单
	*/
	@ResponseBody
	@RequestMapping(params = "optionOrder")
	public String shezhiDingdan(HttpServletRequest request, HttpServletResponse response){
		String orderId= request.getParameter("orderId");

		System.out.println(orderId);

		String option= request.getParameter("option");

		OrderHeadEntity headEntity = systemService.<OrderHeadEntity>findHql("from OrderHeadEntity where orderId= ?", orderId).get(0);

		switch (option){
			case "pay":
				headEntity.setTicketState("已付款");
				break;
			case "cancel":
				headEntity.setTicketState("已取消");
				break;
			case "view":
				headEntity.setTicketState("已观看");
				break;
			default:
				break;
		}
		systemService.save(headEntity);
		return "操作成功";

	}
	/**
	 * 删除订单head
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson shanchu(OrderHeadEntity orderHead, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		orderHead = systemService.getEntity(OrderHeadEntity.class, orderHead.getId());
		message = "订单单头删除成功";
		try{
			orderHeadService.delete(orderHead);
			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单头删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 批量删除订单head
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson plshanchu(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单头删除成功";
		try{
			for(String id:ids.split(",")){
				OrderHeadEntity orderHead = systemService.getEntity(OrderHeadEntity.class, 
				id
				);
				orderHeadService.delete(orderHead);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单头删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加订单head
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson tianjia(OrderHeadEntity orderHead, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单头添加成功";
		try{
			orderHeadService.save(orderHead);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "订单单头添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 更新订单head
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson genxin(OrderHeadEntity orderHead, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "订单单头更新成功";
		OrderHeadEntity t = orderHeadService.get(OrderHeadEntity.class, orderHead.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(orderHead, t);
			orderHeadService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "订单单头更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

}
