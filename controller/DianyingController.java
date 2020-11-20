package com.yyh.movie.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yyh.movie.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
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
//import com.yyh.movie.entity.CgUploadEntity;
//import com.yyh.movie.service.CgFormFieldServiceI;
import com.yyh.movie.service.MovieServiceI;
import com.yyh.movie.service.SystemService;

/**
 * Controller
 * 电影列表
 */

@Controller
@RequestMapping("/movieController")
public class DianyingController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(DianyingController.class);

    @Autowired
    private MovieServiceI movieService;
    @Autowired
    private SystemService systemService;


    /**
     *跳转到首页
     */
    @RequestMapping(params = "index")
    public String shouye(HttpServletRequest request) {
        Session serviceSession = systemService.getSession();

        String hql = " from MovieEntity";
        Query query = serviceSession.createQuery(hql);
        query.setMaxResults(4);
        List<MovieEntity> movies = query.<MovieEntity>list();
        request.setAttribute("movies", movies);


        // 查看电影分类
        TSTypegroup typegroup = systemService.getTypeGroup("movie_type", "电影类型");

        List<TSType> tsTypes = typegroup.getTSTypes();

        request.setAttribute("movieTypes", tsTypes);


        // 动作电影
        Query queryDz = serviceSession.createQuery(" from MovieEntity where movieType like '%动作%' ");
        queryDz.setMaxResults(5);
        List<MovieEntity> dzMovies = queryDz.<MovieEntity>list();
        request.setAttribute("dzMovies", dzMovies);

        // 战争电影
        Query queryZZ = serviceSession.createQuery(" from MovieEntity where movieType like '%战争%' ");
        queryZZ.setMaxResults(5);
        List<MovieEntity> zzMovies = queryZZ.<MovieEntity>list();
        request.setAttribute("zzMovies", zzMovies);

        // 剧情电影
        Query queryJJ = serviceSession.createQuery(" from MovieEntity where movieType like '%剧情%' ");
        queryJJ.setMaxResults(5);
        List<MovieEntity> jqMovies = queryJJ.<MovieEntity>list();
        request.setAttribute("jqMovies", jqMovies);

        // 喜剧电影
        Query queryXj = serviceSession.createQuery(" from MovieEntity where movieType like '%喜剧%' ");
        queryXj.setMaxResults(5);
        List<MovieEntity> xjMovies = queryXj.<MovieEntity>list();
        request.setAttribute("xjMovies", xjMovies);

        // 科幻电影
        Query queryKh = serviceSession.createQuery(" from MovieEntity where movieType like '%科幻%' ");
        queryKh.setMaxResults(5);
        List<MovieEntity> khMovies = queryKh.<MovieEntity>list();
        request.setAttribute("khMovies", khMovies);
        request.getSession().setAttribute("user",request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER));
        return "movie/moviePage/index";
    }

    /**
     * 跳转到电影详情页面
     */
    @RequestMapping(params = "detail")
    public String xiangxi(HttpServletRequest request, String id) {

        MovieEntity movieEntity = systemService.get(MovieEntity.class, id);
        request.setAttribute("movie", movieEntity);

        MovieCommEntity moviecommEntity = systemService.get(MovieCommEntity.class, id);
        request.setAttribute("movieComm", moviecommEntity);

        // 加载其他电影，用于电影推荐
        Session serviceSession = systemService.getSession();
        Query query = serviceSession.createQuery(" from MovieEntity where id !='" + id + "' ");
        query.setMaxResults(2);
        List<MovieEntity> movies = query.<MovieEntity>list();
        request.setAttribute("otherMovies", movies);


        // 查看电影地区
        TSTypegroup typegroupRegion = systemService.getTypeGroup("movie_area", "电影地区");
        List<TSType> tsTypesRegion = typegroupRegion.getTSTypes();
        request.setAttribute("movieRegions", tsTypesRegion);

        // 查看电影语言
        TSTypegroup typegroupLang = systemService.getTypeGroup("movie_lan", "电影语言");
        List<TSType> tsTypesLang = typegroupLang.getTSTypes();
        request.setAttribute("movieLanguages", tsTypesLang);

        // 查看电影分类
        TSTypegroup typegroup = systemService.getTypeGroup("movie_type", "电影类型");

        List<TSType> tsTypes = typegroup.getTSTypes();

        request.setAttribute("movieTypes", tsTypes);


        // 动作电影
        Query queryDz = serviceSession.createQuery(" from MovieEntity where movieType like '%动作%' ");
        queryDz.setMaxResults(5);
        List<MovieEntity> dzMovies = queryDz.<MovieEntity>list();
        request.setAttribute("dzMovies", dzMovies);

        // 战争电影
        Query queryZZ = serviceSession.createQuery(" from MovieEntity where movieType like '%战争%' ");
        queryZZ.setMaxResults(5);
        List<MovieEntity> zzMovies = queryZZ.<MovieEntity>list();
        request.setAttribute("zzMovies", zzMovies);

        // 剧情电影
        Query queryJJ = serviceSession.createQuery(" from MovieEntity where movieType like '%剧情%' ");
        queryJJ.setMaxResults(5);
        List<MovieEntity> jqMovies = queryJJ.<MovieEntity>list();
        request.setAttribute("jqMovies", jqMovies);

        // 喜剧电影
        Query queryXj = serviceSession.createQuery(" from MovieEntity where movieType like '%喜剧%' ");
        queryXj.setMaxResults(5);
        List<MovieEntity> xjMovies = queryXj.<MovieEntity>list();
        request.setAttribute("xjMovies", xjMovies);

        // 科幻电影
        Query queryKh = serviceSession.createQuery(" from MovieEntity where movieType like '%科幻%' ");
        queryKh.setMaxResults(5);
        List<MovieEntity> khMovies = queryKh.<MovieEntity>list();
        request.setAttribute("khMovies", khMovies);

        request.getSession().setAttribute("user",request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER));

        return "movie/moviePage/details";
    }

    /**
     * 购买电影
     */
    @RequestMapping(params = "buyMovie")
    @ResponseBody
    public String goumai(HttpServletRequest request,HttpServletResponse response){
    	
    	TSUser user =(TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);
		if (user == null) {
			//单点登录 - 返回链接
			String returnURL = (String)request.getSession().getAttribute("ReturnURL");
			if(StringUtils.isNotEmpty(returnURL)){
				request.setAttribute("ReturnURL", returnURL);
			}
			return "您还未登录账户，请先登录后再购买！谢谢！";
		}

        String movieId= request.getParameter("movieId");

        MovieEntity movieEntity = new MovieEntity();
        if(StringUtils.isNotEmpty(movieId)){
            movieEntity =systemService.get(MovieEntity.class,movieId);
        }

        //TSUser user = request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER);

        OrderHeadEntity headEntity = new OrderHeadEntity();

        headEntity.setBuyerName(user.getUserName());
        headEntity.setPhone(request.getParameter("phone"));

        headEntity.setTotalPrice(movieEntity.getMoviePrice());
        headEntity.setAddress(movieEntity.getCinema());

        headEntity.setWatchTime(movieEntity.getMovieTimeToMarket());

        headEntity.setOrderId(UUID.randomUUID().toString());
        headEntity.setTicketState("未付款");

        headEntity.setPhone(request.getParameter("phone"));

        headEntity.setCreateBy(user.getUserName());
        headEntity.setCreateName(user.getUserName());
        headEntity.setCreateDate(new Date());
        systemService.save(headEntity);

        OrderBodyEntity bodyEntity = new OrderBodyEntity();

        bodyEntity.setCinema(headEntity.getAddress());

        bodyEntity.setMovieName(movieEntity.getMovieName());

        bodyEntity.setOrderId(headEntity.getOrderId());

        bodyEntity.setPrice(String.valueOf(movieEntity.getMoviePrice()));

        bodyEntity.setSeatL(request.getParameter("seatL"));
        bodyEntity.setSeatP(request.getParameter("seatP"));

        systemService.save(bodyEntity);

        return "电影票购买成功";
    }

    /**
     * 显示电影搜索列表
     */
    @RequestMapping(params = "showMovieList")
    public String xianshiliebiao(HttpServletRequest request, MovieEntity movieEntity) {

        // 查看电影地区
        TSTypegroup typegroupRegion = systemService.getTypeGroup("movie_area", "电影地区");
        List<TSType> tsTypesRegion = typegroupRegion.getTSTypes();
        request.setAttribute("movieRegions", tsTypesRegion);

        // 查看电影语言
        TSTypegroup typegroupLang = systemService.getTypeGroup("movie_lan", "电影语言");
        List<TSType> tsTypesLang = typegroupLang.getTSTypes();
        request.setAttribute("movieLanguages", tsTypesLang);

        // 查看电影分类
        TSTypegroup typegroup = systemService.getTypeGroup("movie_type", "电影类型");
        List<TSType> tsTypes = typegroup.getTSTypes();
        request.setAttribute("movieTypes", tsTypes);

        Session serviceSession = systemService.getSession();
        // 动作电影
        Query queryDz = serviceSession.createQuery(" from MovieEntity where movieType like '%动作%' ");
        queryDz.setMaxResults(5);
        List<MovieEntity> dzMovies = queryDz.<MovieEntity>list();
        request.setAttribute("dzMovies", dzMovies);

        // 战争电影
        Query queryZZ = serviceSession.createQuery(" from MovieEntity where movieType like '%战争%' ");
        queryZZ.setMaxResults(5);
        List<MovieEntity> zzMovies = queryZZ.<MovieEntity>list();
        request.setAttribute("zzMovies", zzMovies);

        // 剧情电影
        Query queryJJ = serviceSession.createQuery(" from MovieEntity where movieType like '%剧情%' ");
        queryJJ.setMaxResults(5);
        List<MovieEntity> jqMovies = queryJJ.<MovieEntity>list();
        request.setAttribute("jqMovies", jqMovies);

        // 喜剧电影
        Query queryXj = serviceSession.createQuery(" from MovieEntity where movieType like '%喜剧%' ");
        queryXj.setMaxResults(5);
        List<MovieEntity> xjMovies = queryXj.<MovieEntity>list();
        request.setAttribute("xjMovies", xjMovies);

        // 科幻电影
        Query queryKh = serviceSession.createQuery(" from MovieEntity where movieType like '%科幻%' ");
        queryKh.setMaxResults(5);
        List<MovieEntity> khMovies = queryKh.<MovieEntity>list();
        request.setAttribute("khMovies", khMovies);

        String hql = " from MovieEntity where 1=1 ";

        if (StringUtils.isNotEmpty(movieEntity.getMovieType())) {
            request.setAttribute("movieType", movieEntity.getMovieType());
            hql += " and movieType like '%" + movieEntity.getMovieType() + "%' ";
        }
        if (StringUtils.isNotEmpty(movieEntity.getMovieName())) {
            request.setAttribute("movieName", movieEntity.getMovieName());
            hql += " and movieName like '%" + movieEntity.getMovieName() + "%' ";
        }
        if (StringUtils.isNotEmpty(movieEntity.getMovieRegion())) {
            request.setAttribute("movieRegion", movieEntity.getMovieRegion());
            hql += " and movieRegion like '%" + movieEntity.getMovieRegion() + "%' ";
        }
        if (StringUtils.isNotEmpty(movieEntity.getMovieLanguage())) {
            request.setAttribute("movieLanguage", movieEntity.getMovieLanguage());
            hql += " and movieLanguage like '%" + movieEntity.getMovieLanguage() + "%' ";
        }

        if (movieEntity.getMoviePrice() != null) {
            hql += " order by moviePrice ";
        }

        if (StringUtils.isNotEmpty(movieEntity.getMovieScore())) {
            hql += " order by movieScore ";
        }

        if (movieEntity.getMovieTimeToMarket() != null) {
            hql += " order by movieTimeToMarket ";
        }

        List<MovieEntity> movieEntities = systemService.<MovieEntity>findHql(hql);
        request.setAttribute("movieEntities", movieEntities);
        request.getSession().setAttribute("user",request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER));
        return "movie/moviePage/movieShowList";
    }

    /**
     * 跳转到电影列表页面
     */
    @RequestMapping(params = "list")
    public ModelAndView liebiao(HttpServletRequest request) {
        return new ModelAndView("movie/dianying");
    }

    /**
     * 收藏
     */
    @RequestMapping(params = "collectionMovieList")
    public ModelAndView shoucangliebiao(HttpServletRequest request) {
        return new ModelAndView("movie/dianyingshoucang");
    }

    /**
     * AJAX请求数据
     * 查看收藏的电影
     */

    @RequestMapping(params = "collectionDatagrid")
    public void collectionDatagrid(MovieEntity movie, HttpServletRequest request,String shoucang, HttpServletResponse response, DataGrid dataGrid) {
        CriteriaQuery cq = new CriteriaQuery(MovieEntity.class, dataGrid);
        //查询条件组装器
        HqlGenerateUtil.installHql(cq, movie, request.getParameterMap());
        List<CollectionEntity> hql = systemService.<CollectionEntity>findHql(" from CollectionEntity  where createBy = ? ", ((TSUser) request.getSession().getAttribute(ResourceUtil.LOCAL_CLINET_USER)).getUserName());

        List<String> idList = new ArrayList<>();
        for (CollectionEntity entity : hql) {
            idList.add(entity.getMovieId());
        }
        if(idList.size()==0&&shoucang.equals("1")){
            TagUtil.datagrid(response, dataGrid);
        }else{
            if(!idList.isEmpty())
                cq.add(Restrictions.in("id", idList.toArray()));
//        cq.add();
            this.movieService.getDataGridReturn(cq, true);

            TagUtil.datagrid(response, dataGrid);
        }
    }


    /**
     * datagrid方法
     */
    @RequestMapping(params = "datagrid")
    public void datagrid(MovieEntity movie, HttpServletRequest request, HttpServletResponse response, DataGrid dataGrid) {
        CriteriaQuery cq = new CriteriaQuery(MovieEntity.class, dataGrid);
        //查询条件组装器
        HqlGenerateUtil.installHql(cq, movie, request.getParameterMap());
        cq.add();
        this.movieService.getDataGridReturn(cq, true);

        TagUtil.datagrid(response, dataGrid);
    }

    /**
     * 删除电影列表
     */
    @RequestMapping(params = "doDel")
    @ResponseBody
    public AjaxJson shanchu(MovieEntity movie, HttpServletRequest request) {
        String message = null;
        AjaxJson j = new AjaxJson();
        movie = systemService.getEntity(MovieEntity.class, movie.getId());
        message = "电影列表删除成功";
        try {
            movieService.delete(movie);
            systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            message = "电影列表删除失败";
            throw new BusinessException(e.getMessage());
        }
        j.setMsg(message);
        return j;
    }

    /**
     * 批量删除电影列表
     */
    @RequestMapping(params = "doBatchDel")
    @ResponseBody
    public AjaxJson piliangshanchu(String ids, HttpServletRequest request) {
        String message = null;
        AjaxJson j = new AjaxJson();
        message = "电影列表删除成功";
        try {
            for (String id : ids.split(",")) {
                MovieEntity movie = systemService.getEntity(MovieEntity.class,
                        id
                );
                movieService.delete(movie);
                systemService.addLog(message, Globals.Log_Type_DEL, Globals.Log_Leavel_INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = "电影列表删除失败";
            throw new BusinessException(e.getMessage());
        }
        j.setMsg(message);
        return j;
    }


    /**
     * 添加电影列表
     */
    @RequestMapping(params = "doAdd")
    @ResponseBody
    public AjaxJson tianjia(MovieEntity movie, HttpServletRequest request) {
        String message = null;
        AjaxJson j = new AjaxJson();
        message = "电影列表添加成功";
        try {
            movieService.save(movie);
            systemService.addLog(message, Globals.Log_Type_INSERT, Globals.Log_Leavel_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            message = "电影列表添加失败";
            throw new BusinessException(e.getMessage());
        }
        j.setMsg(message);
        j.setObj(movie);
        return j;
    }

    /**
     * 更新电影列表
     */
    @RequestMapping(params = "doUpdate")
    @ResponseBody
    public AjaxJson genxin(MovieEntity movie, HttpServletRequest request) {
        String message = null;
        AjaxJson j = new AjaxJson();
        message = "电影列表更新成功";
        MovieEntity t = movieService.get(MovieEntity.class, movie.getId());
        try {
            MyBeanUtils.copyBeanNotNull2Bean(movie, t);
            movieService.saveOrUpdate(t);
            systemService.addLog(message, Globals.Log_Type_UPDATE, Globals.Log_Leavel_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            message = "电影列表更新失败";
            throw new BusinessException(e.getMessage());
        }
        j.setMsg(message);
        return j;
    }
}
