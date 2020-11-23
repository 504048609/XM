<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
<meta charset="utf-8">
<title>注册登录页面</title>
<link rel="stylesheet" href="webpage/movie/login/css/style.css">

<script type="text/javascript" src="plug-in/jquery/jquery-1.8.3.min.js"></script>
<script type="text/javascript" src="plug-in/jquery/jquery.cookie.js"></script>
<script type="text/javascript" src="plug-in/mutiLang/en.js"></script>
<script type="text/javascript" src="plug-in/mutiLang/zh-cn.js"></script>
<script type="text/javascript" src="plug-in/login/js/jquery.tipsy.js"></script>
<script type="text/javascript" src="plug-in/login/js/iphone.check.js"></script>
<script type="text/javascript" src="webpage/login/login-ace.js"></script>
<script type="text/javascript" src="webpage/movie/layui/layui.js"></script>
<link rel="stylesheet" href="webpage/movie/layui/css/layui.css">
</head>

<body>
	<div class="content">

		<div class="form sign-in">
			<h2>欢迎回来</h2>
			<label> <span>用户名</span> <input type="text" name="username"
				id="userName" />
			</label> <label> <span>密码</span> <input type="password" id="password"
				name="password" />
			</label>
			<button type="button" style="margin-left: 155px" class="submit"
				onclick="checkUser()">登 录</button>
		</div>

		<div class="sub-cont">
			<div class="img">
				<div class="img__text m--up">
					<h2>还未注册？</h2>
					<p>立即注册，发现更多好看电影！</p>
				</div>
				<div class="img__text m--in">
					<h2>已有帐号？</h2>
					<p>有帐号就登录吧，更多好看的电影在等在你！</p>
				</div>
				<div class="img__btn">
					<span class="m--up">注 册</span> <span class="m--in">登 录</span>
				</div>
			</div>
			<div class="form sign-up">
				<h2>立即注册</h2>
				<label> <span>用户名</span> <input type="text" id="regUsername" />
				</label> <label> <span>密码</span> <input type="password"
					id="regPassword" />
				</label>
				<button type="button" style="margin-left: 155px" class="submit"
					onclick="register()">注 册</button>
			</div>
		</div>

	</div>

	<script src="webpage/movie/login/js/script.js"></script>

	<div style="text-align: center;"></div>
</body>
<script>
	layui.use('layer', function() {
		var $ = layui.jquery, layer = layui.layer;
	})

	//表单验证
	function validForm() {
		if ($.trim($("#userName").val()).length == 0) {
			layer.alert("请输入用户名")
			return false;
		}

		if ($.trim($("#password").val()).length == 0) {
			layer.alert("请输入密码")
			return false;
		}

		return true;
	}

	//验证用户信息
	function checkUser() {
		if (!validForm()) {
			return false;
		}
		newLogin();
	}

	//登录处理函数
	function newLogin() {
		var username = document.getElementById("userName").value;
		var password = document.getElementById("password").value;
		var actionurl = "movieController.do?index";//提交路径
		var checkurl = "loginController.do?checkuser";//验证路径

		$.ajax({
			async : false,
			cache : false,
			type : 'POST',
			url : checkurl,// 请求的action路径
			data : {
				userName : username,
				password : password
			},
			error : function() {// 请求失败处理函数
			},
			success : function(data) {

				var infoObj = JSON.parse(data)
				if (infoObj.msg == "操作成功") {
					alert("登录成功")
					window.location.href = actionurl;
				} else {
					alert(infoObj.msg)
				}
			}
		});
	}

	//注册代码
	function register() {

		var regUsername = document.getElementById("regUsername").value;
		var regPassword = document.getElementById("regPassword").value;

		if (regUsername == "" || regPassword == "") {
			layer.alert("请完善表单信息！")
		} else {
			$.ajax({
				type : "GET",
				url : "userController.do?saveUser",
				data : {
					userName : regUsername,
					password : regPassword
				},
				dataType : "json",
				success : function(data) {
					alert(data.msg);
					window.location.href = "loginController.do?login";
				}
			});
		}
	}
</script>
</html>