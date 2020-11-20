package com.yyh.movie.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.kisso.SSOHelper;
import com.baomidou.kisso.SSOToken;
import com.baomidou.kisso.common.util.HttpUtil;
import com.yyh.movie.common.ClientManager;
import com.yyh.movie.common.ResourceUtil;
import com.yyh.movie.common.bean.AjaxJson;
import com.yyh.movie.common.bean.Client;
import com.yyh.movie.common.bean.ListtoMenu;
import com.yyh.movie.common.constants.Globals;
import com.yyh.movie.common.constants.SysThemesEnum;
import com.yyh.movie.common.utils.ContextHolderUtils;
import com.yyh.movie.common.utils.JSONHelper;
import com.yyh.movie.common.utils.MutiLangUtil;
import com.yyh.movie.common.utils.OConvertUtils;
import com.yyh.movie.common.utils.PasswordUtil;
import com.yyh.movie.common.utils.SysThemesUtil;
import com.yyh.movie.entity.TSFunction;
import com.yyh.movie.entity.TSPasswordResetkey;
import com.yyh.movie.entity.TSRole;
import com.yyh.movie.entity.TSRoleUser;
import com.yyh.movie.entity.TSUser;
import com.yyh.movie.service.SystemService;
import com.yyh.movie.service.UserService;

import net.sf.json.JSONArray;

/**
 * Controller 登录
 */
@Controller
@RequestMapping("/loginController")
public class LoginController extends BaseController {
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	private SystemService systemService;
	private UserService userService;
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
	 * 跳转到注册页面
	 */
	@RequestMapping(params = "goRegister")
	public String goRegister(HttpServletResponse response, HttpServletRequest request) {
		return "movie/login/login";
	}

	/**
	 * 注册操作
	 */
	@RequestMapping(params = "doRegister")
	@ResponseBody
	public String doRegister(HttpServletResponse response, HttpServletRequest request) {

		return "注册成功";
	}

	/**
	 * 检查用户账号、密码、登录验证码
	 */
	@RequestMapping(params = "checkuser")
	@ResponseBody
	public AjaxJson checkuser(TSUser user, HttpServletRequest req) {
		AjaxJson j = new AjaxJson();
		// 用户登录验证逻辑
		TSUser u = userService.checkUserExits(user);
		if (u == null) {
			u = userService.findUniqueByProperty(TSUser.class, "userName", user.getUserName());
			if (u == null || !u.getPassword()
					.equals(PasswordUtil.encrypt(u.getUserName(), user.getPassword(), PasswordUtil.getStaticSalt()))) {
				j.setMsg("用户名或密码输入错误");
				j.setSuccess(false);
				return j;
			}
		} else {
			HttpSession session = ContextHolderUtils.getSession();
			session.setAttribute(ResourceUtil.LOCAL_CLINET_USER, user);
		}
		return j;
	}

	/**
	 * 获取登录用户的登录信息
	 */
	@RequestMapping(params = "login")
	public String login(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
		TSUser user = (TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user != null) {
			return "main/hplus_main";
		} else {
			String returnURL = (String) request.getSession().getAttribute("ReturnURL");
			if (StringUtils.isNotEmpty(returnURL)) {
				request.setAttribute("ReturnURL", returnURL);
			}
			return "login/login";
		}

	}

	/**
	 * 退出系统
	 */
	@RequestMapping(params = "logout")
	public ModelAndView logout(HttpServletRequest request) {
		HttpSession session = ContextHolderUtils.getSession();
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		try {
			systemService.addLog("用户" + user != null ? user.getUserName() : "" + "已退出", Globals.Log_Type_EXIT,
					Globals.Log_Leavel_INFO);
		} catch (Exception e) {
			// LogUtil.error(e.toString());
		}
		clientManager.removeClinet(session.getId());
		session.invalidate();
		ModelAndView modelAndView = new ModelAndView(new RedirectView("loginController.do?login"));
		return modelAndView;
	}

	/**
	 * 菜单跳转
	 */
	@RequestMapping(params = "left")
	public ModelAndView left(HttpServletRequest request) {
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		HttpSession session = ContextHolderUtils.getSession();
		ModelAndView modelAndView = new ModelAndView();
		// 登陆者的权限
		if (user.getId() == null) {
			session.removeAttribute(Globals.USER_SESSION);
			modelAndView.setView(new RedirectView("loginController.do?login"));
		} else {
			modelAndView.setViewName("main/left");
			request.setAttribute("menuMap", userService.getFunctionMap(user.getId()));
		}
		return modelAndView;
	}

	/**
	 * 首页菜单搜索框自动补全
	 */
	@RequestMapping(params = "getAutocomplete", method = { RequestMethod.GET, RequestMethod.POST })
	public void getAutocomplete(HttpServletRequest request, HttpServletResponse response) {
		String searchVal = request.getParameter("q");
		// 获取到session中的菜单列表
		HttpSession session = ContextHolderUtils.getSession();
		Client client = clientManager.getClient(session.getId());
		// 获取到的是一个map集合
		Map<Integer, List<TSFunction>> map = client.getFunctionMap();
		// 声明list用来存储菜单
		List<TSFunction> autoList = new ArrayList<TSFunction>();
		// 循环map集合取到菜单
		for (int t = 0; t < map.size(); t++) {
			// 根据map键取到菜单的TSFuction 用List接收
			List<TSFunction> list = map.get(t);
			// 循环List取到TSFuction中的functionname
			for (int i = 0; i < list.size(); i++) {
				// 由于functionname中的一些参数没有被国际化，所以还是字母，需要MutiLangUtil中的getLang()方法来
				String name = MutiLangUtil.getLang(list.get(i).getFunctionName());
				if (name.indexOf(searchVal) != -1) {
					TSFunction ts = new TSFunction();
					ts.setFunctionName(MutiLangUtil.getLang(list.get(i).getFunctionName()));
					autoList.add(ts);
				}
			}
		}
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Pragma", "No-cache");
			response.setHeader("Cache-Control", "no-cache");
			response.setDateHeader("Expires", 0);
			response.getWriter().write(JSONHelper.listtojson(new String[] { "functionName" }, 1, autoList));
			response.getWriter().flush();
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			try {
				response.getWriter().close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 获取请求路径
	 */
	@RequestMapping(params = "getUrlpage")
	@ResponseBody
	public String getUrlpage(HttpServletRequest request, HttpServletResponse response) {
		String urlname = request.getParameter("urlname");
		HttpSession session = ContextHolderUtils.getSession();
		Client client = clientManager.getClient(session.getId());
		Map<Integer, List<TSFunction>> map = client.getFunctionMap();
		List<TSFunction> autoList = new ArrayList<TSFunction>();
		for (int t = 0; t < map.size(); t++) {
			List<TSFunction> list = map.get(t);
			for (int i = 0; i < list.size(); i++) {
				String funname = MutiLangUtil.getLang(list.get(i).getFunctionName());
				if (urlname.equals(funname)) {
					TSFunction ts = new TSFunction();
					ts.setFunctionUrl(list.get(i).getFunctionUrl());
					autoList.add(ts);
				}
			}
		}
		if (autoList.size() == 0) {
			return null;
		} else {
			String name = autoList.get(0).getFunctionUrl();
			return name;
		}

	}

	/**
	 * 跳转到密码重置界面
	 */
	@RequestMapping(params = "goResetPwd")
	public ModelAndView goResetPwd(String key) {
		return new ModelAndView("login/resetPwd").addObject("key", key);
	}

	/**
	 * 密码重置
	 */
	@RequestMapping(params = "resetPwd")
	@ResponseBody
	public AjaxJson resetPwd(String key, String password) {
		AjaxJson ajaxJson = new AjaxJson();
		TSPasswordResetkey passwordResetkey = systemService.get(TSPasswordResetkey.class, key);
		Date now = new Date();
		if (passwordResetkey != null && passwordResetkey.getIsReset() != 1
				&& (now.getTime() - passwordResetkey.getCreateDate().getTime()) < 1000 * 60 * 60 * 3) {
			TSUser user = systemService.findUniqueByProperty(TSUser.class, "userName", passwordResetkey.getUsername());
			user.setPassword(PasswordUtil.encrypt(user.getUserName(), password, PasswordUtil.getStaticSalt()));
			systemService.updateEntitie(user);
			passwordResetkey.setIsReset(1);
			systemService.updateEntitie(passwordResetkey);
			ajaxJson.setMsg("密码重置成功");
		} else {
			ajaxJson.setSuccess(false);
			ajaxJson.setMsg("无效重置密码KEY");
		}

		return ajaxJson;
	}

	/**
	 * 跳转到密码重置填写邮箱界面
	 */
	@RequestMapping(params = "goResetPwdMail")
	public ModelAndView goResetPwdMail() {
		return new ModelAndView("login/goResetPwdMail");
	}

	/**
	 * 发送重置密码邮件
	 */
	@RequestMapping(params = "sendResetPwdMail")
	@ResponseBody
	public AjaxJson sendResetPwdMail(String email, HttpServletRequest request) {
		AjaxJson ajaxJson = new AjaxJson();
		try {
			ajaxJson.setSuccess(false);
			ajaxJson.setMsg("邮件地址不能为空");
		} catch (Exception e) {
			if ("javax.mail.AuthenticationFailedException".equals(e.getClass().getName())) {
				ajaxJson.setSuccess(false);
				ajaxJson.setMsg("发送邮件失败：邮箱账号信息设置错误");
				log.error("重置密码发送邮件失败：邮箱账号信息设置错误", e);
			} else {
				ajaxJson.setSuccess(false);
				ajaxJson.setMsg("发送邮件失败：" + e.getMessage());
				log.error("发送邮件失败：" + e.getMessage(), e);
			}

		}
		return ajaxJson;
	}

	@RequestMapping(params = "goPwdInit")
	public String goPwdInit() {
		return "login/pwd_init";
	}

	/**
	 * 首页跳转
	 */
	@RequestMapping(params = "home")
	public ModelAndView home(HttpServletRequest request) {

		SysThemesEnum sysTheme = SysThemesUtil.getSysTheme(request);
		// ACE ACE2 DIY时需要在home.jsp头部引入依赖的js及css文件
		if ("ace".equals(sysTheme.getStyle()) || "diy".equals(sysTheme.getStyle())
				|| "acele".equals(sysTheme.getStyle())) {
			request.setAttribute("show", "1");
		} else {// default及shortcut不需要引入依赖文件，所有需要屏蔽
			request.setAttribute("show", "0");
		}
		return new ModelAndView("main/home");
	}

	/**
	 * ACE首页跳转
	 */
	@RequestMapping(params = "acehome")
	public ModelAndView acehome(HttpServletRequest request) {

		SysThemesEnum sysTheme = SysThemesUtil.getSysTheme(request);
		// ACE ACE2 DIY时需要在home.jsp头部引入依赖的js及css文件
		if ("ace".equals(sysTheme.getStyle()) || "diy".equals(sysTheme.getStyle())
				|| "acele".equals(sysTheme.getStyle())) {
			request.setAttribute("show", "1");
		} else {// default及shortcut不需要引入依赖文件，所有需要屏蔽
			request.setAttribute("show", "0");
		}
		return new ModelAndView("main/acehome");
	}

	/**
	 * HPLUS首页跳转
	 */
	@RequestMapping(params = "hplushome")
	public ModelAndView hplushome(HttpServletRequest request) {
		return new ModelAndView("movie/moviePage/player/index");
	}

	/**
	 * fineUI首页跳转
	 */
	@RequestMapping(params = "fineuiHome")
	public ModelAndView fineuiHome(HttpServletRequest request) {
		return new ModelAndView("main/fineui_home");
	}

	/**
	 * 无权限页面提示跳转
	 */
	@RequestMapping(params = "noAuth")
	public ModelAndView noAuth(HttpServletRequest request) {
		return new ModelAndView("common/noAuth");
	}

	/**
	 * top bootstrap头部菜单请求
	 */
	@RequestMapping(params = "top")
	public ModelAndView top(HttpServletRequest request) {
		TSUser user =(TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		HttpSession session = ContextHolderUtils.getSession();
		// 登陆者的权限
		if (user.getId() == null) {
			session.removeAttribute(Globals.USER_SESSION);
			return new ModelAndView(new RedirectView("loginController.do?login"));
		}
		request.setAttribute("menuMap", userService.getFunctionMap(user.getId()));
		return new ModelAndView("main/bootstrap_top");
	}

	/**
	 * top shortcut头部菜单请求
	 */
	@RequestMapping(params = "shortcut_top")
	public ModelAndView shortcut_top(HttpServletRequest request) {
		TSUser user = (TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		HttpSession session = ContextHolderUtils.getSession();
		// 登陆者的权限
		if (user.getId() == null) {
			session.removeAttribute(Globals.USER_SESSION);
			return new ModelAndView(new RedirectView("loginController.do?login"));
		}
		request.setAttribute("menuMap", userService.getFunctionMap(user.getId()));
		return new ModelAndView("main/shortcut_top");
	}

	@RequestMapping(params = "primaryMenu")
	@ResponseBody
	public String getPrimaryMenu(HttpServletRequest request) {
		List<TSFunction> primaryMenu = userService.getFunctionMap(((TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER)).getId()).get(0);
		// Shortcut一级菜单图标个性化设置（TODO 暂时写死）
		String floor = userService.getShortcutPrimaryMenu(primaryMenu);
		return floor;
	}

	@RequestMapping(params = "primaryMenuDiy")
	@ResponseBody
	public String getPrimaryMenuDiy(HttpServletRequest request) {
		// 取二级菜单
		List<TSFunction> primaryMenu = userService.getFunctionMap(((TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER)).getId()).get(1);
		// Shortcut二级菜单图标个性化设置（TODO 暂时写死）
		String floor = userService.getShortcutPrimaryMenuDiy(primaryMenu);
		return floor;
	}

	/**
	 * 云桌面返回：用户权限菜单
	 */
	@RequestMapping(params = "getPrimaryMenuForWebos")
	@ResponseBody
	public AjaxJson getPrimaryMenuForWebos(HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		// 将菜单加载到Session，用户只在登录的时候加载一次
		Object getPrimaryMenuForWebos = ContextHolderUtils.getSession().getAttribute("getPrimaryMenuForWebos");
		if (OConvertUtils.isNotEmpty(getPrimaryMenuForWebos)) {
			j.setMsg(getPrimaryMenuForWebos.toString());
		} else {
			String PMenu = ListtoMenu.getWebosMenu(userService.getFunctionMap(((TSUser)request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER)).getId()));
			ContextHolderUtils.getSession().setAttribute("getPrimaryMenuForWebos", PMenu);
			j.setMsg(PMenu);
		}
		return j;
	}

	/**
	 * ACE登录界面
	 */
	@RequestMapping(params = "login3")
	public String login3() {
		return "login/login3";
	}

	/**
	 * AdminLTE返回：用户权限菜单
	 */
	@RequestMapping(params = "getPrimaryMenuForAdminlte", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public AjaxJson getPrimaryMenuForAdminlte(String functionId, HttpServletRequest request) {
		AjaxJson j = new AjaxJson();
		try {

			// List<TSFunction> functions =
			// this.systemService.findByProperty(TSFunction.class, "TSFunction.id",
			// functionId);
			String userid =((TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER)).getId();
			List<TSFunction> functions = userService.getSubFunctionList(userid, functionId);

			JSONArray jsonArray = new JSONArray();
			if (functions != null && functions.size() > 0) {
				for (TSFunction function : functions) {
					JSONObject jsonObjectInfo = new JSONObject();
					jsonObjectInfo.put("id", function.getId());

					jsonObjectInfo.put("text",
							OConvertUtils.getString(MutiLangUtil.getLang(function.getFunctionName())));
					jsonObjectInfo.put("url", OConvertUtils.getString(function.getFunctionUrl()));
					jsonObjectInfo.put("targetType", "iframe-tab");
					jsonObjectInfo.put("icon", "fa " + OConvertUtils.getString(function.getFunctionIconStyle()));

					jsonObjectInfo.put("children", getChildOfAdminLteTree(function));
					jsonArray.add(jsonObjectInfo);
				}
			}
			j.setObj(jsonArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return j;
	}

	public JSONArray getChildOfAdminLteTree(TSFunction function) {
		JSONArray jsonArray = new JSONArray();
		List<TSFunction> functions = this.systemService.findByProperty(TSFunction.class, "TSFunction.id",
				function.getId());
		if (functions != null && functions.size() > 0) {
			for (TSFunction tsFunction : functions) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("id", tsFunction.getId());
				jsonObject.put("text", MutiLangUtil.getLang(tsFunction.getFunctionName()));
				jsonObject.put("url", tsFunction.getFunctionUrl());
				jsonObject.put("targetType", "iframe-tab");
				jsonObject.put("icon", "fa " + tsFunction.getFunctionIconStyle());
				jsonObject.put("children", getChildOfAdminLteTree(tsFunction));
				jsonArray.add(jsonObject);
			}
		}
		return jsonArray;
	}

	/**
	 * AdminLTE首页跳转
	 */
	@RequestMapping(params = "adminlteHome")
	public ModelAndView adminlteHome(HttpServletRequest request) {
		return new ModelAndView("main/adminlte_home");
	}

}