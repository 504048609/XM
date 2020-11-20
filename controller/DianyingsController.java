package com.yyh.movie.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yyh.movie.common.ResourceUtil;
import com.yyh.movie.common.utils.ContextHolderUtils;
import com.yyh.movie.entity.OrderHeadEntity;
import com.yyh.movie.entity.TSUser;
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
import com.yyh.movie.entity.MovieCommEntity;
import com.yyh.movie.service.MovieCommServiceI;
import com.yyh.movie.service.SystemService;

/**   
 *Controller
 *电影评价
 */
@Controller
@RequestMapping("/movieCommController")
public class DianyingsController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(DianyingsController.class);

	@Autowired
	private MovieCommServiceI movieCommService;
	@Autowired
	private SystemService systemService;
	


	/**
	 * 电影评价列表
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("movie/dianyings");
	}

	/**
	 * datagrid方法
	 */
	@RequestMapping(params = "datagrid")
	public void datagrid(MovieCommEntity movieComm,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		//从session中拿到数据
		HttpSession session = ContextHolderUtils.getSession();
		TSUser tsUser=(TSUser) session.getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		//判断该用户类型是否为管理员
		if(!tsUser.getUserName().equals("admin")){
			movieComm.setCreateBy(tsUser.getUserName());
		}
		CriteriaQuery cq = new CriteriaQuery(MovieCommEntity.class, dataGrid);
		//查询条件组装器
		HqlGenerateUtil.installHql(cq, movieComm, request.getParameterMap());
		cq.add();
		this.movieCommService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除电影评价
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson shanchu(MovieCommEntity movieComm, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		movieComm = systemService.getEntity(MovieCommEntity.class, movieComm.getId());
		message = "电影评价删除成功";
		try{
			movieCommService.delete(movieComm);
			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "电影评价删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 批量删除电影评价
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson plshanchu(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "电影评价删除成功";
		try{
			for(String id:ids.split(",")){
				MovieCommEntity movieComm = systemService.getEntity(MovieCommEntity.class, 
				id
				);
				movieCommService.delete(movieComm);
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "电影评价删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加电影评价
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson tianjia(MovieCommEntity movieComm, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "电影评价添加成功";
		try{
			movieCommService.save(movieComm);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "电影评价添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}
	
	/**
	 * 更新电影评价
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson genxin(MovieCommEntity movieComm, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "电影评价更新成功";
		MovieCommEntity t = movieCommService.get(MovieCommEntity.class, movieComm.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(movieComm, t);
			movieCommService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "电影评价更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

}
