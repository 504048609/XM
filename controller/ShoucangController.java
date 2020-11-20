package com.yyh.movie.controller;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.yyh.movie.common.utils.ContextHolderUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.yyh.movie.entity.CollectionEntity;
import com.yyh.movie.entity.TSUser;
import com.yyh.movie.service.CollectionServiceI;
import com.yyh.movie.service.SystemService;

/**
 *Controller
 *收藏功能
 */
@Controller
@RequestMapping("/collectionController")
public class ShoucangController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(ShoucangController.class);

	@Autowired
	private CollectionServiceI collectionService;
	@Autowired
	private SystemService systemService;


	/**
	 *收藏电影
	*/
	@RequestMapping(params = "collcetionMovie")
	@ResponseBody
	public String shoucangMovie(HttpServletRequest request, HttpServletResponse response, CollectionEntity collectionEntity){
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user == null) {
			//单点登录 - 返回链接
			String returnURL = (String)request.getSession().getAttribute("ReturnURL");
			if(StringUtils.isNotEmpty(returnURL)){
				request.setAttribute("ReturnURL", returnURL);
			}
			return "您还未登录账户，请先登录后再收藏！谢谢！";
		}

		String movieId = request.getParameter("movieId");

		if(StringUtils.isNotEmpty(movieId)){
			collectionEntity.setMovieId(movieId);
			collectionEntity.setCreateBy(user.getUserName());
			collectionEntity.setCreateName(user.getUserName());
			collectionEntity.setCreateDate(new Date());
	        systemService.save(collectionEntity);
		}
		return "收藏成功";
	}

	/**
	 * 收藏列表分页
	 */
	@RequestMapping(params = "list")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("movie/shoucang");
	}

	/**
	 *datagrid方法
	 */
	@RequestMapping(params = "datagrid")
	public void datagrid(CollectionEntity collection,HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(CollectionEntity.class, dataGrid);
		//查询条件组装器
		HqlGenerateUtil.installHql(cq, collection, request.getParameterMap());
		cq.add();
		this.collectionService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 删除收藏记录
	 */
	@RequestMapping(params = "doDel")
	@ResponseBody
	public AjaxJson shanchu(CollectionEntity collection, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		//collection = systemService.getEntity(CollectionEntity.class, collection.getId());
		message = "收藏删除成功";
		try{
			HttpSession session = ContextHolderUtils.getSession();
			TSUser tsUser=(TSUser) session.getAttribute(ResourceUtil.LOCAL_CLINET_USER);
			collection.setMovieId(collection.getId());
			collection.setId(null);
			collection.setCreateName(tsUser.getUserName());
			List<Object[]> list= systemService.findListbySql("select * from collection where create_by='"+tsUser.getUserName()+"' and movie_id='"+collection.getMovieId()+"'");
			if(list.size()>0){
				Object[] obj=list.get(0);
				collectionService.deleteEntityById(CollectionEntity.class, (Serializable) obj[0]);
			}


			systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "收藏删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * 批量删除收藏记录
	 */
	 @RequestMapping(params = "doBatchDel")
	@ResponseBody
	public AjaxJson plshanchu(String ids,HttpServletRequest request){
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "收藏删除成功";
		try{
			HttpSession session = ContextHolderUtils.getSession();
			TSUser tsUser=(TSUser) session.getAttribute(ResourceUtil.LOCAL_CLINET_USER);
			for(String id:ids.split(",")){
//				CollectionEntity collection = systemService.getEntity(CollectionEntity.class,
//				id
//				);
				CollectionEntity collection=new CollectionEntity();
				collection.setMovieId(id);
				collection.setCreateName(tsUser.getUserName());
				List<Object[]> list= systemService.findListbySql("select * from collection where create_by='"+tsUser.getUserName()+"' and movie_id='"+collection.getMovieId()+"'");
				if(list.size()>0){
					Object[] obj=list.get(0);
					collectionService.deleteEntityById(CollectionEntity.class, (Serializable) obj[0]);
				}
				systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
			}
		}catch(Exception e){
			e.printStackTrace();
			message = "收藏删除失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}


	/**
	 * 添加收藏
	 */
	@RequestMapping(params = "doAdd")
	@ResponseBody
	public AjaxJson shoucang(CollectionEntity collection, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "收藏表添加成功";
		try{
			collectionService.save(collection);
			systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		}catch(Exception e){
			e.printStackTrace();
			message = "收藏表添加失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

	/**
	 * 更新收藏参数
	 */
	@RequestMapping(params = "doUpdate")
	@ResponseBody
	public AjaxJson gengxin(CollectionEntity collection, HttpServletRequest request) {
		String message = null;
		AjaxJson j = new AjaxJson();
		message = "收藏更新成功";
		CollectionEntity t = collectionService.get(CollectionEntity.class, collection.getId());
		try {
			MyBeanUtils.copyBeanNotNull2Bean(collection, t);
			collectionService.saveOrUpdate(t);
			systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			e.printStackTrace();
			message = "收藏更新失败";
			throw new BusinessException(e.getMessage());
		}
		j.setMsg(message);
		return j;
	}

}
