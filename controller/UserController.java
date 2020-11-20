package com.yyh.movie.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Property;
//import org.jeecgframework.web.system.dao.DepartAuthGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yyh.movie.common.ClientManager;
import com.yyh.movie.common.ResourceUtil;
import com.yyh.movie.common.bean.AjaxJson;
import com.yyh.movie.common.bean.ComboBox;
import com.yyh.movie.common.bean.DataGrid;
import com.yyh.movie.common.bean.ListtoMenu;
import com.yyh.movie.common.bean.UploadFile;
import com.yyh.movie.common.bean.ValidForm;
import com.yyh.movie.common.constants.Globals;
import com.yyh.movie.common.constants.OrgConstants;
import com.yyh.movie.common.constants.SysThemesEnum;
import com.yyh.movie.common.hql.CriteriaQuery;
import com.yyh.movie.common.hql.HqlGenerateUtil;
import com.yyh.movie.common.utils.IpUtil;
import com.yyh.movie.common.utils.OConvertUtils;
import com.yyh.movie.common.utils.PasswordUtil;
import com.yyh.movie.common.utils.RoletoJson;
import com.yyh.movie.common.utils.SetListSort;
import com.yyh.movie.common.utils.StringUtil;
import com.yyh.movie.common.utils.SysThemesUtil;
import com.yyh.movie.common.utils.TagUtil;
import com.yyh.movie.entity.InterroleEntity;
import com.yyh.movie.entity.InterroleUserEntity;
import com.yyh.movie.entity.TSDepart;
import com.yyh.movie.entity.TSFunction;
import com.yyh.movie.entity.TSRole;
import com.yyh.movie.entity.TSRoleFunction;
import com.yyh.movie.entity.TSRoleUser;
import com.yyh.movie.entity.TSUser;
import com.yyh.movie.entity.TSUserOrg;
import com.yyh.movie.service.SystemService;
import com.yyh.movie.service.UserService;

/**
 * Controller 用户
 */
@Controller
@RequestMapping("/userController")
public class UserController extends BaseController {
	private static final Logger logger = Logger.getLogger(UserController.class);

	private UserService userService;
	private SystemService systemService;
	@Resource
	private ClientManager clientManager;

	@Autowired
	public void setSystemService(SystemService systemService) {
		this.systemService = systemService;
	}

	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 菜单列表
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(params = "menu")
	public void menu(HttpServletRequest request, HttpServletResponse response) {
		SetListSort sort = new SetListSort();
		TSUser u = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		// 登陆者的权限
		Set<TSFunction> loginActionlist = new HashSet<TSFunction>();// 已有权限菜单
		List<TSRoleUser> rUsers = systemService.findByProperty(TSRoleUser.class, "TSUser.id", u.getId());
		for (TSRoleUser ru : rUsers) {
			TSRole role = ru.getTSRole();
			List<TSRoleFunction> roleFunctionList = systemService.findByProperty(TSRoleFunction.class, "TSRole.id",
					role.getId());
			if (roleFunctionList.size() > 0) {
				for (TSRoleFunction roleFunction : roleFunctionList) {
					TSFunction function = (TSFunction) roleFunction.getTSFunction();
					loginActionlist.add(function);
				}
			}
		}
		List<TSFunction> bigActionlist = new ArrayList<TSFunction>();// 一级权限菜单
		List<TSFunction> smailActionlist = new ArrayList<TSFunction>();// 二级权限菜单
		if (loginActionlist.size() > 0) {
			for (TSFunction function : loginActionlist) {
				if (function.getFunctionLevel() == 0) {
					bigActionlist.add(function);
				} else if (function.getFunctionLevel() == 1) {
					smailActionlist.add(function);
				}
			}
		}
		// 菜单栏排序
		Collections.sort(bigActionlist, sort);
		Collections.sort(smailActionlist, sort);
		String logString = ListtoMenu.getMenu(bigActionlist, smailActionlist);
		// request.setAttribute("loginMenu",logString);
		try {
			response.getWriter().write(logString);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 用户列表页面跳转
	 */
	@RequestMapping(params = "user")
	public String user(HttpServletRequest request) {
		// 给部门查询条件中的下拉框准备数据
		List<TSDepart> departList = systemService.getList(TSDepart.class);
		request.setAttribute("departsReplace", RoletoJson.listToReplaceStr(departList, "departname", "id"));
		departList.clear();
		return "user/index";
	}

	@RequestMapping(params = "interfaceUser")
	public String interfaceUser(HttpServletRequest request) {
		// 给部门查询条件中的下拉框准备数据
		List<TSDepart> departList = systemService.getList(TSDepart.class);
		request.setAttribute("departsReplace", RoletoJson.listToReplaceStr(departList, "departname", "id"));
		departList.clear();
		return "system/user/interfaceUserList";
	}

	@RequestMapping(params = "delInterfaceUser")
	@ResponseBody
	public AjaxJson delInterfaceUser(@RequestParam(required = true) String userid) {
		AjaxJson ajaxJson = new AjaxJson();
		try {
			TSUser user = this.userService.getEntity(TSUser.class, userid);
			if (user != null) {
				String sql = "delete from t_s_interrole_user where user_id = ?";
				this.systemService.executeSql(sql, userid);
				this.userService.delete(user);
				ajaxJson.setMsg("删除成功");
			} else {
				ajaxJson.setMsg("用户不存在");
			}

		} catch (Exception e) {
			// LogUtil.log("删除失败", e.getMessage());
			ajaxJson.setSuccess(false);
			ajaxJson.setMsg(e.getMessage());
		}
		return ajaxJson;
	}

	/**
	 * 用户信息
	 */
	@RequestMapping(params = "userinfo")
	public String userinfo(HttpServletRequest request) {
		TSUser user =(TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);

		user = systemService.getEntity(TSUser.class, user.getId());

		request.setAttribute("user", user);
		return "user/userinfo";
	}

	/**
	 * 修改密码
	 */
	@RequestMapping(params = "changepassword")
	public String changepassword(HttpServletRequest request) {
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		request.setAttribute("user", user);
		// return "system/user/changepassword";
		return "user/changepassword";
	}

	@RequestMapping(params = "changeportrait")
	public String changeportrait(HttpServletRequest request) {
		TSUser user =(TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		request.setAttribute("user", user);
		return "system/user/changeportrait";
	}

	/**
	 * 得到角色列表
	 */
	@RequestMapping(params = "role")
	@ResponseBody
	public List<ComboBox> role(HttpServletResponse response, HttpServletRequest request, ComboBox comboBox) {
		String id = request.getParameter("id");
		List<ComboBox> comboBoxs = new ArrayList<ComboBox>();
		List<TSRole> roles = new ArrayList<TSRole>();
		if (StringUtil.isNotEmpty(id)) {
			List<TSRoleUser> roleUser = systemService.findByProperty(TSRoleUser.class, "TSUser.id", id);
			if (roleUser.size() > 0) {
				for (TSRoleUser ru : roleUser) {
					roles.add(ru.getTSRole());
				}
			}
		}
		List<TSRole> roleList = systemService.getList(TSRole.class);
		comboBoxs = TagUtil.getComboBox(roleList, roles, comboBox);

		roleList.clear();
		roles.clear();

		return comboBoxs;
	}

	/**
	 * 得到部门列表
	 * 
	 * @return
	 */
	@RequestMapping(params = "depart")
	@ResponseBody
	public List<ComboBox> depart(HttpServletResponse response, HttpServletRequest request, ComboBox comboBox) {
		String id = request.getParameter("id");
		List<ComboBox> comboBoxs = new ArrayList<ComboBox>();
		List<TSDepart> departs = new ArrayList();
		if (StringUtil.isNotEmpty(id)) {
			TSUser user = systemService.get(TSUser.class, id);
			// if (user.getTSDepart() != null) {
			// TSDepart depart = systemService.get(TSDepart.class,
			// user.getTSDepart().getId());
			// departs.add(depart);
			// }
			// todo zhanggm 获取指定用户的组织机构列表
			List<TSDepart[]> resultList = systemService
					.findHql("from TSDepart d,TSUserOrg uo where d.id=uo.orgId and uo.id=?", id);
			for (TSDepart[] departArr : resultList) {
				departs.add(departArr[0]);
			}
		}
		List<TSDepart> departList = systemService.getList(TSDepart.class);
		comboBoxs = TagUtil.getComboBox(departList, departs, comboBox);
		return comboBoxs;
	}

	/**
	 * 
	 * 跳转重置用户密码页面
	 */
	@RequestMapping(params = "edit")
	public ModelAndView edit(TSUser user, HttpServletRequest req) {
		logger.info("[" + IpUtil.getIpAddr(req) + "][跳转用户编辑页面][" + user.getUserName() + "]");
		if (StringUtil.isNotEmpty(user.getId())) {
			user = systemService.getEntity(TSUser.class, user.getId());
			req.setAttribute("user", user);
			idandname(req, user);
			// System.out.println(user.getPassword()+"-----"+user.getRealName());
		}
		return new ModelAndView("user/edit");
	}

	/**
	 * 检查用户名
	 * 
	 * @param ids
	 * @return
	 */
	@RequestMapping(params = "checkUser")
	@ResponseBody
	public ValidForm checkUser(HttpServletRequest request) {
		ValidForm v = new ValidForm();
		String userName = OConvertUtils.getString(request.getParameter("param"));
		String code = OConvertUtils.getString(request.getParameter("code"));
		List<TSUser> roles = systemService.findByProperty(TSUser.class, "userName", userName);
		if (roles.size() > 0 && !code.equals(userName)) {
			v.setInfo("用户名已存在");
			v.setStatus("n");
		}
		return v;
	}

	/**
	 * 检查用户邮箱
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "checkUserEmail")
	@ResponseBody
	public ValidForm checkUserEmail(HttpServletRequest request) {
		ValidForm validForm = new ValidForm();
		String email = OConvertUtils.getString(request.getParameter("param"));
		String code = OConvertUtils.getString(request.getParameter("code"));
		List<TSUser> userList = systemService.findByProperty(TSUser.class, "email", email);
		if (userList.size() > 0 && !code.equals(email)) {
			validForm.setInfo("邮箱已绑定相关用户信息");
			validForm.setStatus("n");
		}
		return validForm;
	}

	/**
	 * 注册
	 * 
	 * @param user
	 * @param req
	 * @return
	 */
	@RequestMapping(params = "saveUser")
	@ResponseBody
	public AjaxJson saveUser(HttpServletRequest req, TSUser user) {
		String message = null;
		AjaxJson j = new AjaxJson();
		Short logType = Globals.Log_Type_UPDATE;
		TSUser users = systemService.findUniqueByProperty(TSUser.class, "userName",user.getUserName());
		if (users != null) {
			message = "用户: " + users.getUserName() + "已经存在";
		} else {
			user.setPassword(PasswordUtil.encrypt(user.getUserName(),
					OConvertUtils.getString(req.getParameter("password")), PasswordUtil.getStaticSalt()));
			user.setUserName(req.getParameter("userName"));
			this.userService.saveOrUpdate(user);
			message = "用户: " + user.getUserName() + "注册成功";
			logType = Globals.Log_Type_INSERT;
		}
		systemService.addLog(message, logType, Globals.Log_Leavel_INFO);
		j.setMsg(message);
		logger.info("[" + IpUtil.getIpAddr(req) + "][添加编辑用户]" + message);
		return j;

	}

	/**
	 * 注册
	 * 
	 * @param request
	 *            request
	 * @param user
	 *            user
	 */
	private void saveUserOrgList(HttpServletRequest request, TSUser user) {
		String orgIds = OConvertUtils.getString(request.getParameter("orgIds"));

		List<TSUserOrg> userOrgList = new ArrayList<TSUserOrg>();
		List<String> orgIdList = extractIdListByComma(orgIds);
		for (String orgId : orgIdList) {
			TSDepart depart = new TSDepart();
			depart.setId(orgId);

			TSUserOrg userOrg = new TSUserOrg();
			userOrg.setTsUser(user);
			userOrg.setTsDepart(depart);

			userOrgList.add(userOrg);
		}
		if (!userOrgList.isEmpty()) {
			systemService.batchSave(userOrgList);
		}
	}

	protected void saveRoleUser(TSUser user, String roleidstr) {
		String[] roleids = roleidstr.split(",");
		for (int i = 0; i < roleids.length; i++) {
			TSRoleUser rUser = new TSRoleUser();
			TSRole role = systemService.getEntity(TSRole.class, roleids[i]);
			rUser.setTSRole(role);
			rUser.setTSUser(user);
			systemService.save(rUser);

		}
	}

	/**
	 * 用户选择角色跳转页面
	 * 
	 * @return
	 */
	@RequestMapping(params = "roles")
	public ModelAndView roles(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView("system/user/users");
		String ids = OConvertUtils.getString(request.getParameter("ids"));
		mv.addObject("ids", ids);
		return mv;
	}

	/**
	 * 角色显示列表
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 */
	@RequestMapping(params = "datagridRole")
	public void datagridRole(TSRole tsRole, HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(TSRole.class, dataGrid);
		// 查询条件组装器

		cq.eq("roleType", OrgConstants.SYSTEM_ROLE_TYPE);// 默认只查询系统角色

		HqlGenerateUtil.installHql(cq, tsRole);
		this.systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * easyuiAJAX请求数据： 用户选择角色列表
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */
	@RequestMapping(params = "addorupdate")
	public ModelAndView addorupdate(TSUser user, HttpServletRequest req) {

		/*
		 * List<TSDepart> departList = new ArrayList<TSDepart>(); String departid =
		 * oConvertUtils.getString(req.getParameter("departid"));
		 * if(!StringUtil.isEmpty(departid)){
		 * departList.add((TSDepart)systemService.getEntity(TSDepart.class,departid));
		 * }else { departList.addAll((List)systemService.getList(TSDepart.class)); }
		 * req.setAttribute("departList", departList);
		 */

		List<String> orgIdList = new ArrayList<String>();
		TSDepart tsDepart = new TSDepart();
		if (StringUtil.isNotEmpty(user.getId())) {
			user = systemService.getEntity(TSUser.class, user.getId());

			req.setAttribute("user", user);
			idandname(req, user);
			getOrgInfos(req, user);

		} else {
			// 组织机构关联用户录入
			String departid = OConvertUtils.getString(req.getParameter("departid"));
			if (StringUtils.isNotEmpty(departid)) {
				TSDepart depart = systemService.getEntity(TSDepart.class, departid);
				if (depart != null) {
					req.setAttribute("orgIds", depart.getId() + ",");
					req.setAttribute("departname", depart.getDepartname() + ",");
				}
			}
			// 角色管理关联用户录入
			String roleId = OConvertUtils.getString(req.getParameter("roleId"));
			if (StringUtils.isNotEmpty(roleId)) {
				TSRole tsRole = systemService.getEntity(TSRole.class, roleId);
				if (tsRole != null) {
					req.setAttribute("id", roleId);
					req.setAttribute("roleName", tsRole.getRoleName());
				}
			}
		}

		req.setAttribute("tsDepart", tsDepart);
		// req.setAttribute("orgIdList", JSON.toJSON(orgIdList));

		return new ModelAndView("system/user/user");
	}

	/**
	 * 添加、编辑接口用户
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */
	@RequestMapping(params = "addorupdateInterfaceUser")
	public ModelAndView addorupdateInterfaceUser(TSUser user, HttpServletRequest req) {

		if (StringUtil.isNotEmpty(user.getId())) {
			user = systemService.getEntity(TSUser.class, user.getId());
			req.setAttribute("user", user);
			interfaceroleidandname(req, user);
		} else {
			String roleId = req.getParameter("roleId");
			if (StringUtils.isNotBlank(roleId)) {
				InterroleEntity role = systemService.getEntity(InterroleEntity.class, roleId);
				req.setAttribute("roleId", roleId);
				req.setAttribute("roleName", role.getRoleName());
			}
		}

		return new ModelAndView("system/user/interfaceUser");
	}

	public void interfaceroleidandname(HttpServletRequest req, TSUser user) {
		List<InterroleUserEntity> roleUsers = systemService.findByProperty(InterroleUserEntity.class, "TSUser.id",
				user.getId());
		String roleId = "";
		String roleName = "";
		if (roleUsers.size() > 0) {
			for (InterroleUserEntity interroleUserEntity : roleUsers) {
				roleId += interroleUserEntity.getInterroleEntity().getId() + ",";
				roleName += interroleUserEntity.getInterroleEntity().getRoleName() + ",";
			}
		}
		req.setAttribute("roleId", roleId);
		req.setAttribute("roleName", roleName);

	}

	protected void saveInterfaceRoleUser(TSUser user, String roleidstr) {
		String[] roleids = roleidstr.split(",");
		for (int i = 0; i < roleids.length; i++) {
			InterroleUserEntity rUser = new InterroleUserEntity();
			InterroleEntity role = systemService.getEntity(InterroleEntity.class, roleids[i]);
			rUser.setInterroleEntity(role);
			rUser.setTSUser(user);
			systemService.save(rUser);
		}
	}

	/**
	 * 用户的登录后的组织机构选择页面
	 * 
	 * @param request
	 *            request
	 * @return 用户选择组织机构页面
	 */
	@RequestMapping(params = "userOrgSelect")
	public ModelAndView userOrgSelect(HttpServletRequest request) {
		List<TSDepart> orgList = new ArrayList<TSDepart>();
		String userId = OConvertUtils.getString(request.getParameter("userId"));

		List<Object[]> orgArrList = systemService.findHql(
				"from TSDepart d,TSUserOrg uo where d.id=uo.tsDepart.id and uo.tsUser.id=?", new String[] { userId });
		for (Object[] departs : orgArrList) {
			orgList.add((TSDepart) departs[0]);
		}
		request.setAttribute("orgList", orgList);

		TSUser user = systemService.getEntity(TSUser.class, userId);
		request.setAttribute("user", user);

		return new ModelAndView("system/user/userOrgSelect");
	}

	public void idandname(HttpServletRequest req, TSUser user) {
		List<TSRoleUser> roleUsers = systemService.findByProperty(TSRoleUser.class, "TSUser.id", user.getId());
		String roleId = "";
		String roleName = "";
		if (roleUsers.size() > 0) {
			for (TSRoleUser tRoleUser : roleUsers) {
				roleId += tRoleUser.getTSRole().getId() + ",";
				roleName += tRoleUser.getTSRole().getRoleName() + ",";
			}
		}
		req.setAttribute("id", roleId);
		req.setAttribute("roleName", roleName);

	}

	public void getOrgInfos(HttpServletRequest req, TSUser user) {
		List<TSUserOrg> tSUserOrgs = systemService.findByProperty(TSUserOrg.class, "tsUser.id", user.getId());
		String orgIds = "";
		String departname = "";
		if (tSUserOrgs.size() > 0) {
			for (TSUserOrg tSUserOrg : tSUserOrgs) {
				orgIds += tSUserOrg.getTsDepart().getId() + ",";
				departname += tSUserOrg.getTsDepart().getDepartname() + ",";
			}
		}
		req.setAttribute("orgIds", orgIds);
		req.setAttribute("departname", departname);

	}

	/**
	 * 根据部门和角色选择用户跳转页面
	 */
	@RequestMapping(params = "choose")
	public String choose(HttpServletRequest request) {
		List<TSRole> roles = systemService.loadAll(TSRole.class);
		request.setAttribute("roleList", roles);
		return "system/membership/checkuser";
	}

	/**
	 * 部门和角色选择用户的panel跳转页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "chooseUser")
	public String chooseUser(HttpServletRequest request) {
		String departid = request.getParameter("departid");
		String roleid = request.getParameter("roleid");
		request.setAttribute("roleid", roleid);
		request.setAttribute("departid", departid);
		return "system/membership/userlist";
	}

	/**
	 * 部门和角色选择用户的用户显示列表
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 */
	@RequestMapping(params = "datagridUser")
	public void datagridUser(HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		String departid = request.getParameter("departid");
		String roleid = request.getParameter("roleid");
		CriteriaQuery cq = new CriteriaQuery(TSUser.class, dataGrid);
		if (departid.length() > 0) {
			cq.eq("TDepart.departid", OConvertUtils.getInt(departid, 0));
			cq.add();
		}
		String userid = "";
		if (roleid.length() > 0) {
			List<TSRoleUser> roleUsers = systemService.findByProperty(TSRoleUser.class, "TRole.roleid",
					OConvertUtils.getInt(roleid, 0));
			if (roleUsers.size() > 0) {
				for (TSRoleUser tRoleUser : roleUsers) {
					userid += tRoleUser.getTSUser().getId() + ",";
				}
			}
			cq.in("userid", OConvertUtils.getInts(userid.split(",")));
			cq.add();
		}
		this.systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	/**
	 * 根据部门和角色选择用户跳转页面
	 */
	@RequestMapping(params = "roleDepart")
	public String roleDepart(HttpServletRequest request) {
		List<TSRole> roles = systemService.loadAll(TSRole.class);
		request.setAttribute("roleList", roles);
		return "system/membership/roledepart";
	}

	/**
	 * 部门和角色选择用户的panel跳转页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "chooseDepart")
	public ModelAndView chooseDepart(HttpServletRequest request) {
		String nodeid = request.getParameter("nodeid");
		ModelAndView modelAndView = null;
		if (nodeid.equals("role")) {
			modelAndView = new ModelAndView("system/membership/users");
		} else {
			modelAndView = new ModelAndView("system/membership/departList");
		}
		return modelAndView;
	}

	/**
	 * 部门和角色选择用户的用户显示列表
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 */
	@RequestMapping(params = "datagridDepart")
	public void datagridDepart(HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
		CriteriaQuery cq = new CriteriaQuery(TSDepart.class, dataGrid);
		systemService.getDataGridReturn(cq, true);
		TagUtil.datagrid(response, dataGrid);
	}

	// /**
	// * 测试 【Datatable 数据列表】
	// *
	// * @param user
	// * @param req
	// * @return
	// */
	// @RequestMapping(params = "test")
	// public void test(HttpServletRequest request, HttpServletResponse response) {
	// String jString = request.getParameter("_dt_json");
	// DataTables dataTables = new DataTables(request);
	// CriteriaQuery cq = new CriteriaQuery(TSUser.class, dataTables);
	// String username = request.getParameter("userName");
	// if (username != null) {
	// cq.like("userName", username);
	// cq.add();
	// }
	// DataTableReturn dataTableReturn = systemService.getDataTableReturn(cq, true);
	// TagUtil.datatable(response, dataTableReturn,
	// "id,userName,mobilePhone,TSDepart_departname");
	// }

	/**
	 * 用户列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "index")
	public String index() {
		return "bootstrap/main";
	}

	/**
	 * 用户列表页面跳转
	 * 
	 * @return
	 */
	@RequestMapping(params = "main")
	public String main() {
		return "bootstrap/test";
	}

	/**
	 * 测试
	 * 
	 * @return
	 */
	@RequestMapping(params = "testpage")
	public String testpage(HttpServletRequest request) {
		return "test/test";
	}

	/**
	 * 设置签名跳转页面
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(params = "addsign")
	public ModelAndView addsign(HttpServletRequest request) {
		String id = request.getParameter("id");
		request.setAttribute("id", id);
		return new ModelAndView("system/user/usersign");
	}

	/**
	 * 用户录入
	 * 
	 * @param user
	 * @param req
	 * @return
	 */

	@RequestMapping(params = "savesign", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson savesign(HttpServletRequest req) {
		String message = null;
		UploadFile uploadFile = new UploadFile(req);
		String id = uploadFile.get("id");
		TSUser user = systemService.getEntity(TSUser.class, id);
		uploadFile.setRealPath("signatureFile");
		uploadFile.setCusPath("signature");
		uploadFile.setByteField("signature");
		uploadFile.setBasePath("resources");
		uploadFile.setRename(false);
		uploadFile.setObject(user);
		AjaxJson j = new AjaxJson();
		message = user.getUserName() + "设置签名成功";
		systemService.uploadFile(uploadFile);
		systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
		j.setMsg(message);

		return j;
	}

	@RequestMapping(params = "changestyle")
	public String changeStyle(HttpServletRequest request) {
		TSUser user =(TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user == null) {
			return "login/login";
		}
		// String indexStyle = "shortcut";
		// String cssTheme="";
		// Cookie[] cookies = request.getCookies();
		// for (Cookie cookie : cookies) {
		// if(cookie==null || StringUtils.isEmpty(cookie.getName())){
		// continue;
		// }
		// if(cookie.getName().equalsIgnoreCase("JEECGINDEXSTYLE")){
		// indexStyle = cookie.getValue();
		// }
		// if(cookie.getName().equalsIgnoreCase("JEECGCSSTHEME")){
		// cssTheme = cookie.getValue();
		// }
		// }
		SysThemesEnum sysThemesEnum = SysThemesUtil.getSysTheme(request);
		request.setAttribute("indexStyle", sysThemesEnum.getStyle());
		// request.setAttribute("cssTheme", cssTheme);
		return "system/user/changestyle";
	}

	/**
	 * @Title: saveStyle @Description: 修改首页样式 @param request @return
	 * AjaxJson @throws
	 */
	@RequestMapping(params = "savestyle")
	@ResponseBody
	public AjaxJson saveStyle(HttpServletRequest request, HttpServletResponse response) {
		AjaxJson j = new AjaxJson();
		j.setSuccess(Boolean.FALSE);
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user != null) {
			String indexStyle = request.getParameter("indexStyle");
			// String cssTheme = request.getParameter("cssTheme");

			// if(StringUtils.isNotEmpty(cssTheme)){
			// Cookie cookie4css = new Cookie("JEECGCSSTHEME", cssTheme);
			// cookie4css.setMaxAge(3600*24*30);
			// response.addCookie(cookie4css);
			// logger.info("cssTheme:"+cssTheme);
			// }else if("ace".equals(indexStyle)){
			// Cookie cookie4css = new Cookie("JEECGCSSTHEME", "metro");
			// cookie4css.setMaxAge(3600*24*30);
			// response.addCookie(cookie4css);
			// logger.info("cssTheme:metro");

			// }else {
			// Cookie cookie4css = new Cookie("JEECGCSSTHEME", "");
			// cookie4css.setMaxAge(3600*24*30);
			// response.addCookie(cookie4css);
			// logger.info("cssTheme:default");
			// }

			if (StringUtils.isNotEmpty(indexStyle)) {
				Cookie cookie = new Cookie("JEECGINDEXSTYLE", indexStyle);
				// 设置cookie有效期为一个月
				cookie.setMaxAge(3600 * 24 * 30);
				response.addCookie(cookie);
				logger.debug(" ----- 首页样式: indexStyle ----- " + indexStyle);
				j.setSuccess(Boolean.TRUE);
				j.setMsg("样式修改成功，请刷新页面");
			}

			try {
				clientManager.getClient().getFunctions().clear();
			} catch (Exception e) {
			}

		} else {
			j.setMsg("请登录后再操作");
		}
		return j;
	}

	/**
	 * 导入功能跳转
	 *
	 * @return
	 */
	@RequestMapping(params = "upload")
	public ModelAndView upload(HttpServletRequest req) {
		req.setAttribute("controller_name", "userController");
		return new ModelAndView("common/upload/pub_excel_upload");
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXls")
	public String exportXls(TSUser tsUser, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid,
			ModelMap modelMap) {
		return "";
		// CriteriaQuery cq = new CriteriaQuery(TSUser.class, dataGrid);
		// org.jeecgframework.core.extend.hqlsearch.HqlGenerateUtil.installHql(cq,
		// tsUser, request.getParameterMap());
		// List<TSUser> tsUsers = this.userService.getListByCriteriaQuery(cq,false);
		// //导出的时候处理一下组织机构编码和角色编码
		// for(int i=0;i<tsUsers.size();i++){
		// TSUser user = tsUsers.get(i);
		// //托管
		// systemService.getSession().evict(user);
		// String id = user.getId();
		//
		// String queryRole = "select * from t_s_role where id in (select roleid from
		// t_s_role_user where userid=:userid)";
		// List<TSRole> roles =
		// systemService.getSession().createSQLQuery(queryRole).addEntity(TSRole.class).setString("userid",id).list();
		// String roleCodes = "";
		// for(TSRole role:roles){
		// roleCodes += ","+role.getRoleCode();
		// }
		// user.setUserKey(roleCodes.replaceFirst(",", ""));
		// String queryDept = "select * from t_s_depart where id in (select org_id from
		// t_s_user_org where user_id=:userid)";
		// List<TSDepart> departs =
		// systemService.getSession().createSQLQuery(queryDept).addEntity(TSDepart.class).setString("userid",id).list();
		// String departCodes = "";
		// for(TSDepart depart:departs){
		// departCodes += ","+depart.getOrgCode();
		// }
		// user.setDepartid(departCodes.replaceFirst(",", ""));
		//
		// }
		// modelMap.put(NormalExcelConstants.FILE_NAME,"用户表");
		// modelMap.put(NormalExcelConstants.CLASS,TSUser.class);
		// modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("用户表列表",
		// "导出人:"+request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER).getRealName(),
		// "导出信息"));
		// modelMap.put(NormalExcelConstants.DATA_LIST,tsUsers);
		// return NormalExcelConstants.JEECG_EXCEL_VIEW;
	}

	/**
	 * 导出excel 使模板
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(params = "exportXlsByT")
	public String exportXlsByT(TSUser tsUser, HttpServletRequest request, HttpServletResponse response,
			DataGrid dataGrid, ModelMap modelMap) {
		// modelMap.put(NormalExcelConstants.FILE_NAME,"用户表");
		// modelMap.put(NormalExcelConstants.CLASS,TSUser.class);
		// modelMap.put(NormalExcelConstants.PARAMS,new ExportParams("用户表列表",
		// "导出人:"+request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER).getRealName(),
		// "导出信息"));
		// modelMap.put(NormalExcelConstants.DATA_LIST,new ArrayList());
		// return NormalExcelConstants.JEECG_EXCEL_VIEW;
		return "";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(params = "importExcel", method = RequestMethod.POST)
	@ResponseBody
	public AjaxJson importExcel(HttpServletRequest request, HttpServletResponse response) {
		// AjaxJson j = new AjaxJson();
		//
		// MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)
		// request;
		// Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		// for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
		// MultipartFile file = entity.getValue();// 获取上传文件对象
		// ImportParams params = new ImportParams();
		// params.setTitleRows(2);
		// params.setHeadRows(1);
		// params.setNeedSave(true);
		// try {
		// List<TSUser> tsUsers =
		// ExcelImportUtil.importExcel(file.getInputStream(),TSUser.class,params);
		// for (TSUser tsUser : tsUsers) {
		// String username = tsUser.getUserName();
		// if(username==null||username.equals("")){
		// j.setMsg("用户名为必填字段，导入失败");
		// return j;
		// }
		//
		// tsUser.setStatus(new Short("1"));
		// tsUser.setDevFlag("0");
		// tsUser.setDeleteFlag(new Short("0"));
		// String roleCodes = tsUser.getUserKey();
		// String deptCodes = tsUser.getDepartid();
		//
		// tsUser.setPassword(PasswordUtil.encrypt(username, "123456",
		// PasswordUtil.getStaticSalt()));
		// tsUser.setUserType(Globals.USER_TYPE_SYSTEM);//导入用户 在用户管理列表不显示
		//
		// if((roleCodes==null||roleCodes.equals(""))||(deptCodes==null||deptCodes.equals(""))){
		// List<TSUser> users =
		// systemService.findByProperty(TSUser.class,"userName",username);
		// if(users.size()!=0){
		// //用户存在更新
		// TSUser user = users.get(0);
		// MyBeanUtils.copyBeanNotNull2Bean(tsUser,user);
		// user.setDepartid(null);
		// systemService.saveOrUpdate(user);
		// }else{
		// tsUser.setDepartid(null);
		// systemService.save(tsUser);
		// }
		// }else{
		// String[] roles = roleCodes.split(",");
		// String[] depts = deptCodes.split(",");
		// boolean flag = true;
		// //判断组织机构编码和角色编码是否存在，如果不存在，也不能导入
		// for(String roleCode:roles){
		// List<TSRole> roleList =
		// systemService.findByProperty(TSRole.class,"roleCode",roleCode);
		// if(roleList.size()==0){
		// flag = false;
		// }
		// }
		//
		// for(String deptCode:depts){
		// List<TSDepart> departList =
		// systemService.findByProperty(TSDepart.class,"orgCode",deptCode);
		// if(departList.size()==0){
		// flag = false;
		// }
		// }
		//
		// if(flag){
		// //判断用户是否存在
		// List<TSUser> users =
		// systemService.findByProperty(TSUser.class,"userName",username);
		// if(users.size()!=0){
		// //用户存在更新
		// TSUser user = users.get(0);
		// MyBeanUtils.copyBeanNotNull2Bean(tsUser,user);
		// user.setDepartid(null);
		// systemService.saveOrUpdate(user);
		//
		// String id = user.getId();
		// systemService.executeSql("delete from t_s_role_user where userid = ?",id);
		// for(String roleCode:roles){
		// //根据角色编码得到roleid
		// List<TSRole> roleList =
		// systemService.findByProperty(TSRole.class,"roleCode",roleCode);
		// TSRoleUser tsRoleUser = new TSRoleUser();
		// tsRoleUser.setTSUser(user);
		// tsRoleUser.setTSRole(roleList.get(0));
		// systemService.save(tsRoleUser);
		// }
		//
		// systemService.executeSql("delete from t_s_user_org where user_id = ?",id);
		// for(String orgCode:depts){
		// //根据角色编码得到roleid
		// List<TSDepart> departList =
		// systemService.findByProperty(TSDepart.class,"orgCode",orgCode);
		// TSUserOrg tsUserOrg = new TSUserOrg();
		// tsUserOrg.setTsDepart(departList.get(0));
		// tsUserOrg.setTsUser(user);
		// systemService.save(tsUserOrg);
		// }
		// }else{
		// //不存在则保存
		// //TSUser user = users.get(0);
		// tsUser.setDepartid(null);
		// systemService.save(tsUser);
		// for(String roleCode:roles){
		// //根据角色编码得到roleid
		// List<TSRole> roleList =
		// systemService.findByProperty(TSRole.class,"roleCode",roleCode);
		// TSRoleUser tsRoleUser = new TSRoleUser();
		// tsRoleUser.setTSUser(tsUser);
		// tsRoleUser.setTSRole(roleList.get(0));
		// systemService.save(tsRoleUser);
		// }
		//
		// for(String orgCode:depts){
		// //根据角色编码得到roleid
		// List<TSDepart> departList =
		// systemService.findByProperty(TSDepart.class,"orgCode",orgCode);
		// TSUserOrg tsUserOrg = new TSUserOrg();
		// tsUserOrg.setTsDepart(departList.get(0));
		// tsUserOrg.setTsUser(tsUser);
		// systemService.save(tsUserOrg);
		// }
		// }
		// j.setMsg("文件导入成功！");
		// }else {
		// j.setMsg("组织机构编码和角色编码不能匹配");
		// }
		// }
		// }
		// } catch (Exception e) {
		// j.setMsg("文件导入失败！");
		// logger.error(e.getMessage());
		// }finally{
		// try {
		// file.getInputStream().close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// return j;
		return null;
	}

	/**
	 * 选择用户跳转页面
	 *
	 * @return
	 */
	@RequestMapping(params = "userSelect")
	public String userSelect() {
		return "system/user/userSelect";
	}

	/**
	 * 添加、编辑我的机构用户
	 * 
	 * @param request
	 * @param response
	 * @param dataGrid
	 * @param user
	 */
	@RequestMapping(params = "addorupdateMyOrgUser")
	public ModelAndView addorupdateMyOrgUser(TSUser user, HttpServletRequest req) {
		List<String> orgIdList = new ArrayList<String>();
		TSDepart tsDepart = new TSDepart();
		if (StringUtil.isNotEmpty(user.getId())) {
			user = systemService.getEntity(TSUser.class, user.getId());

			req.setAttribute("user", user);
			idandname(req, user);
			getOrgInfos(req, user);
		} else {
			String departid = OConvertUtils.getString(req.getParameter("departid"));
			TSDepart org = systemService.getEntity(TSDepart.class, departid);
			req.setAttribute("orgIds", departid);
			req.setAttribute("departname", org.getDepartname());
		}
		req.setAttribute("tsDepart", tsDepart);
		return new ModelAndView("system/user/myOrgUser");
	}
}